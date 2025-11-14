package com.example.listitaapp.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class ClearOnUnauthorizedInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            // Clear token immediately so UI observers can react
            runBlocking { tokenManager.clearAuth() }
        }
        return response
    }
}


