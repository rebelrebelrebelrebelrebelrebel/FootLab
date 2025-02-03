package com.example.footlab

import ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL =  " https://2c21-2806-2f0-9181-8aa8-2178-193d-edc9-224a.ngrok-free.app/clasificar" // Your Flask server URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .readTimeout(60, TimeUnit.SECONDS)    // Configuración de tiempo de lectura
        .connectTimeout(60, TimeUnit.SECONDS) // Configuración de tiempo de conexión
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Agrega el cliente con el interceptor de logging y los tiempos de espera
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
