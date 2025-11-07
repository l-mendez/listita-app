package com.example.listitaapp.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip adding token for public endpoints (login, register, verify)
        val isPublicEndpoint = request.url.encodedPath.let { path ->
            path.contains("/login") ||
            path.contains("/register") ||
            path.contains("/verify-account") ||
            path.contains("/forgot-password") ||
            path.contains("/reset-password")
        }

        if (isPublicEndpoint) {
            return chain.proceed(request)
        }

        // Add authorization header with JWT token
        val token = tokenManager.getTokenSync()
        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
