import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Data class for the request body
data class ImageRequest(val imageUrl: String)

// Data class for the API response
data class ClassificationResponse(val prediction: String, val confidence: Float)

// Interface for the API service
interface ApiService {
    @POST("/clasificar") // Change the endpoint to match your Flask API
    fun classifyImage(@Body request: ImageRequest): Call<ClassificationResponse>
}

// Function to create Retrofit instance
fun createRetrofit(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://7e0e-2806-2f0-92c0-6a47-a8da-3783-a4a-6f58.ngrok-free.app") // Replace with your Flask API base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ApiService::class.java)
}

// Example function to call the classifyImage API
fun classifyImage(imageUrl: String) {
    val apiService = createRetrofit()
    val imageRequest = ImageRequest(imageUrl)

    apiService.classifyImage(imageRequest).enqueue(object : Callback<ClassificationResponse> {
        override fun onResponse(call: Call<ClassificationResponse>, response: Response<ClassificationResponse>) {
            if (response.isSuccessful) {
                // Handle the successful response
                val classificationResponse = response.body()
                classificationResponse?.let {
                    // Access the prediction and confidence
                    println("Prediction: ${it.prediction}, Confidence: ${it.confidence}")
                }
            } else {
                // Handle the error response
                println("Error: ${response.code()} - ${response.message()}")
            }
        }

        override fun onFailure(call: Call<ClassificationResponse>, t: Throwable) {
            // Handle the failure (e.g., network error)
            println("Failure: ${t.message}")
        }
    })
}
