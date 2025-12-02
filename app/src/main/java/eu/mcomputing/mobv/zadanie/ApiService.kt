package eu.mcomputing.mobv.zadanie

import android.content.Context
import retrofit2.Call
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

data class UserRegistration(val name: String, val email: String, val password: String)
data class RegistrationResponse(val uid: String, val access: String, val refresh: String)

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

data class UserLoginRequest(val name: String, val password: String)
data class LoginResponse(val uid: String, val access: String, val refresh: String)

data class UserResponse(
    val id: String,
    val name: String,
    val photo: String
)

data class RefreshTokenRequest(val refresh: String)
data class RefreshTokenResponse(
    val uid: String,
    val access: String,
    val refresh: String
)

interface ApiService {

    @Headers("x-apikey: c95332ee022df8c953ce470261efc695ecf3e784")
    @POST("user/create.php")
    suspend fun registerUser(@Body userInfo: UserRegistration): Response<RegistrationResponse>

    @Headers("x-apikey: c95332ee022df8c953ce470261efc695ecf3e784")
    @POST("user/login.php")
    suspend fun loginUser(@Body userInfo: UserLoginRequest): Response<LoginResponse>
    @Headers("x-apikey: c95332ee022df8c953ce470261efc695ecf3e784")
    @GET("geofence/list.php")
    suspend fun listGeofence(): Response<GeofenceResponse>

    @GET("user/get.php")
    suspend fun getUser(
        @HeaderMap header: Map<String, String>,
        @Query("id") id: String
    ): Response<UserResponse>

    @POST("user/refresh.php")
    suspend fun refreshToken(
        @HeaderMap header: Map<String, String>,
        @Body refreshInfo: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @POST("user/refresh.php")
    fun refreshTokenBlocking(
        @Body refreshInfo: RefreshTokenRequest
    ): Call<RefreshTokenResponse>

    companion object {
        fun create(context: Context): ApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .authenticator(TokenAuthenticator(context))
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://zadanie.mpage.sk/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
