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
        if (username.isEmpty()) return Pair("Používateľské meno nemôže byť prázdne", null)
        if (email.isEmpty()) return Pair("Email nemôže byť prázdny", null)
        if (password.isEmpty()) return Pair("Heslo nemôže byť prázdne", null)

        if (cache.getUserByUsername(username) != null) return Pair("Používateľské meno už existuje", null)
        if (cache.getUserByEmail(email) != null) return Pair("Email už existuje", null)

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
                    return Pair("Registrácia bola úspešná! Môžete sa prihlásiť.", newUser)
                }
            }
            return Pair("Nepodarilo sa vytvoriť používateľa", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Nepodarilo sa vytvoriť používateľa", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return Pair("Fatal error. Nepodarilo sa vytvoriť používateľa.", null)
    }


    suspend fun apiLoginUser(email: String, password: String): Pair<String, User?> {
        if (email.isEmpty()) return Pair("Email nemôže byť prázdny", null)
        if (password.isEmpty()) return Pair("Heslo nemôže byť prázdne", null)

        try {
            val userEntity = cache.getUserByEmail(email)
            if (userEntity == null) return Pair("nenašiel sa žiadny používateľ s týmto emailom", null)

            val hashedInput = hashPassword(password)

            return if (userEntity.password == hashedInput) {
                val loggedInUser = User(userEntity.username, userEntity.email, userEntity.uid, "", "")
                Pair("Prihlásenie úspešné", loggedInUser)
            } else {
                Pair("Chybné údaje", null)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return Pair("Fatal error. Nepodarilo sa prihlásiť.", null)
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
            return "Nepodarilo sa získať používateľov"
        } catch (ex: IOException) {
            ex.printStackTrace()
            return "Nepodarilo sa získať používateľov"
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Fatal error. Nepodarilo sa získať používateľov."
    }

    suspend fun logoutUser() {
        cache.logoutUser()
    }

    fun getUsers() = cache.getUsers()
}
