package com.example.listitaapp.di

import android.content.Context
import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.api.AuthInterceptor
import com.example.listitaapp.data.api.ClearOnUnauthorizedInterceptor
import com.example.listitaapp.data.api.TokenManager
import com.example.listitaapp.data.datasource.AuthRemoteDataSource
import com.example.listitaapp.data.datasource.CategoryRemoteDataSource
import com.example.listitaapp.data.datasource.ListItemRemoteDataSource
import com.example.listitaapp.data.datasource.ProductRemoteDataSource
import com.example.listitaapp.data.datasource.PurchaseRemoteDataSource
import com.example.listitaapp.data.datasource.ShoppingListRemoteDataSource
import com.example.listitaapp.data.datasource.UserRemoteDataSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenManager: TokenManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(ClearOnUnauthorizedInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideShoppingListRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): ShoppingListRemoteDataSource {
        return ShoppingListRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun provideListItemRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): ListItemRemoteDataSource {
        return ListItemRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): ProductRemoteDataSource {
        return ProductRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun provideCategoryRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): CategoryRemoteDataSource {
        return CategoryRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun providePurchaseRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): PurchaseRemoteDataSource {
        return PurchaseRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): AuthRemoteDataSource {
        return AuthRemoteDataSource(apiService, moshi)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        apiService: ApiService,
        moshi: Moshi
    ): UserRemoteDataSource {
        return UserRemoteDataSource(apiService, moshi)
    }
}
