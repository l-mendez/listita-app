package com.example.listitaapp.data.api

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object ApiClient {

    // Base URL for the API
    // Use 10.0.2.2 for Android Emulator, localhost for physical device on same network
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Timeout durations
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private var apiService: ApiService? = null
    private var tokenManager: TokenManager? = null

    /**
     * Initialize the API client with application context
     */
    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    /**
     * Get TokenManager instance
     */
    fun getTokenManager(context: Context): TokenManager {
        if (tokenManager == null) {
            tokenManager = TokenManager(context.applicationContext)
        }
        return tokenManager!!
    }

    /**
     * Build and configure OkHttpClient
     */
    private fun buildOkHttpClient(context: Context): OkHttpClient {
        val tokenMgr = getTokenManager(context)

        // Logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Auth interceptor to add JWT token
        val authInterceptor = AuthInterceptor(tokenMgr)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private fun buildJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    /**
     * Build and configure Retrofit
     */
    private fun buildRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient(context))
            .addConverterFactory(buildJson().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Get ApiService instance (Singleton)
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            apiService = buildRetrofit(context).create(ApiService::class.java)
        }
        return apiService!!
    }

    /**
     * Clear the API service instance (useful for testing or logout)
     */
    fun clearInstance() {
        apiService = null
    }
}
