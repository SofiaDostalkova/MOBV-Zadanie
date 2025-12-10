package eu.mcomputing.mobv.zadanie

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

class DataRepository private constructor(
    private val service: ApiService,
    private val uploadService: UploadService,
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
                        ApiService.create(context),
                        UploadService.create(),
                        LocalCache(AppRoomDatabase.getInstance(context).appDao())
                    ).also { INSTANCE = it }
            }
    }
    suspend fun apiListGeofence(accessToken: String): String {
        try {
            val headers = mapOf(
                "x-apikey" to "c95332ee022df8c953ce470261efc695ecf3e784",
                "Authorization" to "Bearer $accessToken"
            )
            val response = service.listGeofence(headers)
            if (response.isSuccessful) {
                response.body()?.let { geofenceResponse ->
                    val users = geofenceResponse.list.map { geofenceUser ->
                        UserEntity(
                            uid = geofenceUser.uid,
                            name = geofenceUser.name,
                            updated = geofenceUser.updated,
                            lat = geofenceResponse.me.lat,
                            lon = geofenceResponse.me.lon,
                            radius = geofenceUser.radius,
                            photo = geofenceUser.photo
                        )
                    }
                    cache.clearUsers()
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

    suspend fun apiLoginUser(
        username: String,
        password: String
    ): Pair<String, User?> {
        if (username.isEmpty()) {
            return Pair("Username can not be empty", null)
        }
        if (password.isEmpty()) {
            return Pair("Password can not be empty", null)
        }
        try {
            val response = service.loginUser(UserLoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { json_response ->
                    if (json_response.uid == "-1") {
                        return Pair("Wrong password or username.", null)
                    }
                    return Pair(
                        "",
                        User(
                            username,
                            "",
                            json_response.uid,
                            json_response.access,
                            json_response.refresh
                        )
                    )
                }
            }
            return Pair("Failed to login user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to login user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to login user.", null)
    }

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

    suspend fun apiGetUser(
        uid: String,
        my_uid: String,
        accessToken: String,
        refreshToken: String
    ): Pair<String, User?> {
        try {
            val response = service.getUser(
                mapOf(
                    "x-apikey" to "c95332ee022df8c953ce470261efc695ecf3e784",
                    "Authorization" to "Bearer $accessToken"
                ), uid
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    return Pair(
                        "",
                        User(
                            it.name,
                            "",
                            it.id,
                            accessToken,
                            refreshToken,
                            it.photo
                        )
                    )
                }
            }

            if (response.code() == 401) {
                val refreshResponse = service.refreshToken(
                    mapOf(
                        "x-apikey" to AppConfig.API_KEY,
                        "x-user" to my_uid
                    ), RefreshTokenRequest(refreshToken)
                )
                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()?.let { newtoken ->
                        val response2 = service.getUser(
                            mapOf(
                                "x-apikey" to AppConfig.API_KEY,
                                "Authorization" to "Bearer ${newtoken.access}"
                            ), uid
                        )
                        if (response2.isSuccessful) {
                            response2.body()?.let {
                                return Pair(
                                    "",
                                    User(
                                        it.name,
                                        "",
                                        it.id,
                                        newtoken.access,
                                        newtoken.refresh,
                                        it.photo
                                    )
                                )
                            }
                        }
                    }
                }
            }
            return Pair("Failed to load user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to load user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to load user.", null)
    }

    suspend fun apiUpdateGeofence(
        lat: Double,
        lon: Double,
        radius: Int,
        accessToken: String
    ): Boolean {
        return try {
            val headers = mapOf(
                "x-apikey" to AppConfig.API_KEY,
                "Authorization" to "Bearer $accessToken"
            )
            val response = service.updateGeofence(
                headers,
                UpdateGeofenceRequest(lat, lon, radius)
            )
            response.isSuccessful && response.body()?.success == true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    suspend fun apiDeleteGeofence(accessToken: String): Boolean {
        return try {
            val headers = mapOf(
                "x-apikey" to AppConfig.API_KEY,
                "Authorization" to "Bearer $accessToken"
            )
            val response = service.deleteGeofence(headers)
            response.isSuccessful && response.body()?.success == true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    suspend fun clearCachedUsers() {
        cache.deleteUserItems()
    }

    suspend fun uploadUserPhoto(
        accessToken: String,
        file: File
    ): Pair<Boolean, String?> {

        val body = file.asRequestBody("image/jpeg".toMediaType())
        val multipart = MultipartBody.Part.createFormData("image", file.name, body)

        val headers = mapOf(
            "Authorization" to "Bearer $accessToken",
            "x-apikey" to AppConfig.API_KEY
        )

        val response = uploadService.uploadImage(headers, multipart)

        return if (response.isSuccessful) {
            val newPhoto = response.body()?.photo
            Pair(true, newPhoto)
        } else {
            Pair(false, null)
        }
    }

    suspend fun deleteUserPhoto(accessToken: String): Pair<Boolean, String?> {
        val headers = mapOf(
            "Authorization" to "Bearer $accessToken",
            "x-apikey" to AppConfig.API_KEY
        )

        val response = uploadService.deletePhoto(headers)

        return if (response.isSuccessful) {
            Pair(true, response.body()?.photo)
        } else Pair(false, null)
    }

    suspend fun apiRequestPasswordReset(email: String): Boolean {
        return try {
            val headers = mapOf("x-apikey" to AppConfig.API_KEY)
            val response = service.requestPasswordReset(headers, ForgotPasswordRequest(email))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun apiChangePassword(accessToken: String, oldPassword: String, newPassword: String): Boolean {
        return try {
            val headers = mapOf(
                "x-apikey" to AppConfig.API_KEY,
                "Authorization" to "Bearer $accessToken"
            )
            val response = service.changePassword(headers, ChangePasswordRequest(oldPassword, newPassword))
            response.isSuccessful && response.body()?.get("status") == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



}