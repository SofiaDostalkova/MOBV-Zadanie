package eu.mcomputing.mobv.zadanie

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

        val token = PreferenceData.getInstance().getUser(context)?.access
        token?.let {
            request.header("Authorization", "Bearer $it")
        }

        // Add API key
        request.addHeader("x-apikey", AppConfig.API_KEY)

        return chain.proceed(request.build())
    }
}