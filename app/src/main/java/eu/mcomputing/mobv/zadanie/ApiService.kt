package eu.mcomputing.mobv.zadanie

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.GET

data class UserRegistration(val name: String, val email: String, val password: String)
data class RegistrationResponse(val uid: String, val access: String, val refresh: String)

data class User(val username: String, val email: String, val uid: String, val access: String, val refresh: String)

data class GeofenceUser(
    val uid: String,
    val name: String,
    val updated: String,
    val lat: Double,
    val lon: Double,
    val radius: Double,
    val photo: String
)

data class GeofenceResponse(
    val list: List<GeofenceUser>
)

interface ApiService {

    @Headers("x-apikey: c95332ee022df8c953ce470261efc695ecf3e784")
    @POST("user/create.php")
    suspend fun registerUser(@Body userInfo: UserRegistration): Response<RegistrationResponse>

    @GET("geofence/list.php")
    suspend fun listGeofence(): Response<GeofenceResponse>
    companion object {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://zadanie.mpage.sk/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
