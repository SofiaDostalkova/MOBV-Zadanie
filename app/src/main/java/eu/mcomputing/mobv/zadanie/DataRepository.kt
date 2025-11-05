package eu.mcomputing.mobv.zadanie

import android.content.Context
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class DataRepository private constructor(
    private val service: ApiService,
    private val cache: LocalCache
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
                    LocalCache(AppRoomDatabase.getInstance(context).appDao())
                ).also { INSTANCE = it }
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun apiRegisterUser(username: String, email: String, password: String): Pair<String, User?> {
        if (username.isEmpty()) return Pair("Username cannot be empty", null)
        if (email.isEmpty()) return Pair("Email cannot be empty", null)
        if (password.isEmpty()) return Pair("Password cannot be empty", null)

        if (cache.getUserByUsername(username) != null) return Pair("Username already exists", null)
        if (cache.getUserByEmail(email) != null) return Pair("Email already exists", null)

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
                    return Pair("Registration successful! You can log in now.", newUser)
                }
            }
            return Pair("Failed to create user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to create user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return Pair("Fatal error. Failed to create user.", null)
    }


    suspend fun apiLoginUser(email: String, password: String): Pair<String, User?> {
        if (email.isEmpty()) return Pair("Email cannot be empty", null)
        if (password.isEmpty()) return Pair("Password cannot be empty", null)

        try {
            val userEntity = cache.getUserByEmail(email)
            if (userEntity == null) return Pair("No user found with this email", null)

            val hashedInput = hashPassword(password)

            return if (userEntity.password == hashedInput) {
                val loggedInUser = User(userEntity.username, userEntity.email, userEntity.uid, "", "")
                Pair("Login successful", loggedInUser)
            } else {
                Pair("Invalid credentials", null)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return Pair("Fatal error. Failed to login.", null)
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
            return "Failed to fetch users"
        } catch (ex: IOException) {
            ex.printStackTrace()
            return "Check internet connection. Failed to fetch users."
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Fatal error. Failed to fetch users."
    }

    fun getUsers() = cache.getUsers()
}
