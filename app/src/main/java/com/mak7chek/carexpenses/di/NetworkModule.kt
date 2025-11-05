// di/NetworkModule.kt
package com.mak7chek.carexpenses.di // Замініть на ваш пакет

import com.mak7chek.carexpenses.data.local.SessionManager
import com.mak7chek.carexpenses.data.network.ApiService
import com.mak7chek.carexpenses.data.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ------------------- !! ВАЖЛИВО !! -------------------
    // Знайдіть свій IP на Arch Linux (команда: ip a)
    // і вставте його сюди.
    private const val BASE_URL = "http://192.168.0.139:8080/"
    // -----------------------------------------------------

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        // Цей логгер буде показувати тіла запитів і відповідей у Logcat.
        // Незамінна річ для дебагінгу.
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Додаємо наш "фейс-контроль"
            .addInterceptor(loggingInterceptor) // Додаємо логгер
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Використовуємо наш кастомний клієнт
            .addConverterFactory(GsonConverterFactory.create()) // Парсер JSON
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}