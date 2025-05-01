package com.example.footlab

import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Data class for the request body
data class ImageRequest(val imageUrl: String)

// Data class for the API response
data class ClassificationResponse(val prediction: String, val confidence: Float)

// Define the service interface in ApiService
interface ApiService {
    @POST("/analizar")
    suspend fun analizar(@Body requestBody: RequestBody): Response<JsonObject>
}


// Rename the Retrofit client object to avoid redeclaration
object ApiClient {
    private const val BASE_URL = "https://7c70-2806-2f0-9181-8aa8-4432-551c-94dd-e92d.ngrok-free.app/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .readTimeout(240, TimeUnit.SECONDS)
        .connectTimeout(240, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// Example function to call the classifyImage API
fun classifyImage(imageUrl: String) {
    // Using CoroutineScope to launch a coroutine
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Use ApiClient to access the apiService
            val json = JsonObject().apply {
                addProperty("image_url", imageUrl)
            }

            // Call the API using the suspend function `analizar`
            val response = ApiClient.apiService.analizar(json.toString().toRequestBody())

            // Handle the response on the main thread
            withContext(Dispatchers.Main) {
                if (response != null) {
                    println("Response: $response")
                    // Process the response here (e.g., update UI, etc.)
                } else {
                    println("Error: Response is null")
                }
            }
        } catch (e: Exception) {
            // Handle errors (e.g., network errors, timeouts)
            withContext(Dispatchers.Main) {
                println("Error: ${e.message}")
            }
        }
    }
}
