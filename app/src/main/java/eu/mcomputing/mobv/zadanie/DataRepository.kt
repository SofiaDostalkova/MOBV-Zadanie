package eu.mcomputing.mobv.zadanie

import android.content.Context
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class DataRepository private constructor(
    private val service: ApiService,
    private val cache: LocalCache,
    private val context: Context
) {

    companion object {
        const val TAG = "DataRepository"

        @Volatile
        private var INSTANCE: DataRepository? = null
        private val lock = Any()

        fun getInstance(context: Context): DataRepository =
            INSTANCE ?: synchronized(lock) {
                INSTANCE ?: DataRepository(
                    ApiService.create(),
                    LocalCache(AppRoomDatabase.getInstance(context).appDao()),
                    context.applicationContext
                ).also { INSTANCE = it }
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun apiRegisterUser(username: String, email: String, password: String): Pair<String, User?> {
        if (username.isEmpty()) return Pair( context.getString(R.string.empty_username), null)
        if (email.isEmpty()) return Pair(context.getString(R.string.empty_email), null)
        if (password.isEmpty()) return Pair(context.getString(R.string.empty_password), null)

        if (cache.getUserByUsername(username) != null) return Pair(context.getString(R.string.user_exists), null)
        if (cache.getUserByEmail(email) != null) return Pair(context.getString(R.string.email_exists), null)

        try {
            val response = service.registerUser(UserRegistration(username, email, password))
            if (response.isSuccessful) {
                response.body()?.let { json ->
                    val newUser = User(username, email, json.uid, json.access, json.refresh)
                    val hashedPassword = hashPassword(password)
                    val newUserEntity = UserEntity(
                        username = username,
                        email = email,
                        password = hashedPassword
                    )
                    cache.insertUserItems(listOf(newUserEntity))
                    return Pair(context.getString(R.string.register_success), newUser)
                }
            }
            return Pair(context.getString(R.string.register_fail), null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair(context.getString(R.string.register_fail), null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return Pair(context.getString(R.string.register_fail), null)
    }


    suspend fun apiLoginUser(email: String, password: String): Pair<String, User?> {
        if (email.isEmpty()) return Pair(context.getString(R.string.empty_email), null)
        if (password.isEmpty()) return Pair(context.getString(R.string.empty_password), null)

        try {
            val userEntity = cache.getUserByEmail(email)
            if (userEntity == null) return Pair(context.getString(R.string.user_not_found), null)

            val hashedInput = hashPassword(password)

            return if (userEntity.password == hashedInput) {
                val loggedInUser = User(userEntity.username, userEntity.email, userEntity.uid, "", "")
                Pair(context.getString(R.string.login_success), loggedInUser)
            } else {
                Pair(context.getString(R.string.login_fail), null)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return Pair(context.getString(R.string.login_error), null)
        }
    }

    suspend fun apiGetUsers(): String {
        try {
            val response = service.getUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    val entities = users.map {
                        UserEntity(it.uid, it.username, it.email, "")
                    }
                    cache.insertUserItems(entities)
                    return ""
                }
            }
            return context.getString(R.string.get_users_fail)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return context.getString(R.string.get_users_fail)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return context.getString(R.string.get_users_fail)
    }

    suspend fun logoutUser() {
        cache.logoutUser()
    }

    fun getUsers() = cache.getUsers()
}
