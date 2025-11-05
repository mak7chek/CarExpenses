package com.mak7chek.carexpenses.data.network
import com.mak7chek.carexpenses.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()


        val token = runBlocking {
            sessionManager.getAuthToken().firstOrNull()
        }

        // Якщо це запит на /auth, токен не потрібен
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        // Якщо токен є, додаємо заголовок
        val newRequestBuilder = originalRequest.newBuilder()
        if (token != null) {
            newRequestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(newRequestBuilder.build())
    }
}