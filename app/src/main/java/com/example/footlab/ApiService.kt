import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class para el cuerpo de la solicitud
data class ImageRequest(val imageUrl: String)

// Interfaz para la API
interface ApiService {
    @POST("/clasificar") // Change the endpoint to match your Flask API
    fun classifyImage(@Body request: ImageRequest): Call<String> // Adjust the response type as needed
}
