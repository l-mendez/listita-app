package com.example.listitaapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.listitaapp.BuildConfig
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
import com.example.listitaapp.data.repository.ThemePreferencesRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
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
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings_preferences") }
        )
    }

    @Provides
    @Singleton
    fun provideThemePreferencesRepository(
        dataStore: DataStore<Preferences>
    ): ThemePreferencesRepository {
        return ThemePreferencesRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideShoppingListRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): ShoppingListRemoteDataSource {
        return ShoppingListRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun provideListItemRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): ListItemRemoteDataSource {
        return ListItemRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): ProductRemoteDataSource {
        return ProductRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun provideCategoryRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): CategoryRemoteDataSource {
        return CategoryRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun providePurchaseRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): PurchaseRemoteDataSource {
        return PurchaseRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): AuthRemoteDataSource {
        return AuthRemoteDataSource(apiService, json)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        apiService: ApiService,
        json: Json
    ): UserRemoteDataSource {
        return UserRemoteDataSource(apiService, json)
    }
}
