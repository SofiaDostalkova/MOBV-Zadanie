package eu.mcomputing.mobv.zadanie

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UpdatePhotoResponse(
    val id: Int,
    val name: String,
    val photo: String
)

interface UploadService {

    @Multipart
    @POST("user/photo.php")
    suspend fun uploadImage(
        @HeaderMap headers: Map<String, String>,
        @Part image: MultipartBody.Part
    ): Response<UpdatePhotoResponse>

    @DELETE("user/photo.php")
    suspend fun deletePhoto(
        @HeaderMap headers: Map<String, String>
    ): Response<UpdatePhotoResponse>

    companion object {
        fun create(): UploadService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://upload.mcomputing.eu/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(UploadService::class.java)
        }
    }
}
