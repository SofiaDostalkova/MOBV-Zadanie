package eu.mcomputing.mobv.zadanie

import android.content.Context
import eu.mcomputing.mobv.zadanie.PreferenceData
import eu.mcomputing.mobv.zadanie.ApiService
import eu.mcomputing.mobv.zadanie.RefreshTokenRequest
import eu.mcomputing.mobv.zadanie.User
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {

        // Skip refresh for login/create/refresh endpoints
        val path = response.request.url.toUrl().path
        if (path.contains("/user/create.php") || path.contains("/user/login.php") || path.contains("/user/refresh.php")) {
            return null
        }

        // Only handle 401
        if (response.code == 401) {
            val user = PreferenceData.getInstance().getUser(context)
            user?.let {
                val tokenResponse = ApiService.create(context).refreshTokenBlocking(
                    RefreshTokenRequest(it.refresh)
                ).execute()

                if (tokenResponse.isSuccessful) {
                    tokenResponse.body()?.let { newTokens ->
                        val updatedUser = User(
                            it.username,
                            it.email,
                            it.id,
                            newTokens.access,
                            newTokens.refresh,
                            it.photo
                        )
                        PreferenceData.getInstance().putUser(context, updatedUser)

                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${updatedUser.access}")
                            .build()
                    }
                }
            }

            // Logout user if refresh fails
            PreferenceData.getInstance().clearData(context)
        }

        return null
    }
}