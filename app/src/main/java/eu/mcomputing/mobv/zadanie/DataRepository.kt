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
                INSTANCE
                    ?: DataRepository(
                        ApiService.create(),
                        LocalCache(AppRoomDatabase.getInstance(context).appDao())
                    ).also { INSTANCE = it }
            }
    }
    suspend fun apiListGeofence(): String {
        try {
            val response = service.listGeofence()

            if (response.isSuccessful) {
                response.body()?.let { geofenceResponse ->
                    val users = geofenceResponse.list.map { geofenceUser ->
                        UserEntity(
                            uid = geofenceUser.uid,
                            name = geofenceUser.name,
                            updated = geofenceUser.updated,
                            lat = geofenceUser.lat,
                            lon = geofenceUser.lon,
                            radius = geofenceUser.radius,
                            photo = geofenceUser.photo
                        )
                    }
                    cache.insertUserItems(users)
                    return ""
                }
            }

            return "Failed to load users"
        } catch (ex: IOException) {
            ex.printStackTrace()
            return "Check internet connection. Failed to load users."
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Fatal error. Failed to load users."
    }

    fun getUsers() = cache.getUsers()
    suspend fun apiRegisterUser(username: String, email: String, password: String) : Pair<String,User?>{
        if (username.isEmpty()){
            return Pair("Username can not be empty", null)
        }
        if (email.isEmpty()){
            return Pair("Email can not be empty", null)
        }
        if (password.isEmpty()){
            return Pair("Password can not be empty", null)
        }
        try {
            val response = service.registerUser(UserRegistration(username, email, password))
            if (response.isSuccessful) {
                response.body()?.let { json_response ->
                    return Pair("", User(username,email,json_response.uid, json_response.access, json_response.refresh))
                }
            }
            return Pair("Failed to create user", null)
        }catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to create user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to create user.", null)
    }
}