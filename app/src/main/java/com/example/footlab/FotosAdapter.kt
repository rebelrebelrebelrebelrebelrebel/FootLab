package com.example.footlab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import retrofit2.Response
import com.example.footlab.RetrofitClient.apiService as apiService1

class FotosAdapter(
    private val context: Context,
    private val fotosUrls: List<String>,
    private val interpreter: Interpreter,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FotosAdapter.FotosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false)
        return FotosViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotosViewHolder, position: Int) {
        val url = fotosUrls[position]

        Glide.with(context)
            .load(url)
            .into(holder.imagenFotoItem)

        holder.botonClasificarItem.setOnClickListener {
            Log.d("FotosAdapter", "Classifying image with URL: $url")
            onItemClick(url)
            classifyImage(url)
        }
    }

    override fun getItemCount(): Int = fotosUrls.size

    private fun classifyImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            showToast("Image URL is empty.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("image_url", imageUrl)
                }
                val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val response: Response<JsonObject> = apiService1.analizar(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            handleClassificationResponse(responseBody)
                        } else {
                            Log.e("FotosAdapter", "Response body is null")
                            showToast("Failed to get a valid response.")
                        }
                    } else {
                        Log.e("FotosAdapter", "Classification failed with code: ${response.code()}")
                        showToast("Classification failed with code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FotosAdapter", "Classification error: ${e.message}")
                showToast("Failed to classify image.")
            }
        }
    }

    private fun handleClassificationResponse(responseBody: JsonObject) {
        Log.d("FotosAdapter", "Response body: $responseBody")

        val yellowPercentage = responseBody.get("Porcentaje de amarillo")?.asDouble ?: 0.0
        val bluePercentage = responseBody.get("Porcentaje de azul")?.asDouble ?: 0.0
        val redPercentage = responseBody.get("Porcentaje de rojo")?.asDouble ?: 0.0
        val callosoPercentage = responseBody.get("Porcentaje de calloso")?.asDouble ?: 0.0
        val fibrinaPercentage = responseBody.get("Porcentaje de fibrina")?.asDouble ?: 0.0
        val granulationPercentage = responseBody.get("Porcentaje de granulación")?.asDouble ?: 0.0

        val predictionsImageClasificar = responseBody.get("predictions_image_clasificar")?.asString ?: ""
        val predictionsImageSegmentacion = responseBody.get("predictions_image_segmentacion")?.asString ?: ""

        val classificationResultsBundle = Bundle().apply {
            putDouble("Porcentaje de amarillo", yellowPercentage)
            putDouble("Porcentaje de azul", bluePercentage)
            putDouble("Porcentaje de rojo", redPercentage)
            putDouble("Porcentaje de calloso", callosoPercentage)
            putDouble("Porcentaje de fibrina", fibrinaPercentage)
            putDouble("Porcentaje de granulación", granulationPercentage)
        }

        val intent = Intent(context, ClassificationActivity::class.java).apply {
            putExtra("BASE64_IMAGE", predictionsImageClasificar)
            putExtra("BASE64_SEGMENTACION", predictionsImageSegmentacion)
            putExtra("CLASSIFICATION_RESULTS", classificationResultsBundle)
        }

        context.startActivity(intent)
    }

    private fun showToast(message: String) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    inner class FotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenFotoItem: ImageView = itemView.findViewById(R.id.imagenFotoItem)
        val botonClasificarItem: Button = itemView.findViewById(R.id.botonClasificarItem)
    }
}
