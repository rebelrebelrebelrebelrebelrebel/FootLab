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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.IOException
import kotlin.concurrent.thread

class FotosAdapter(
    private val context: Context,
    private val fotosUrls: List<String>,
    private val interpreter: Interpreter,
    private val onClasificarClick: (String) -> Unit
) : RecyclerView.Adapter<FotosAdapter.FotosViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false)
        return FotosViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotosViewHolder, position: Int) {
        val url = fotosUrls[position]

        // Load the image using Glide
        Glide.with(context)
            .load(url)
            .into(holder.imagenFotoItem)

        // Set up button click listeners
        holder.botonClasificarItem.setOnClickListener {
            Log.d("FotosAdapter", "Classifying image with URL: $url")
            classifyImage(url) // Pass the URL directly
        }
    }

    override fun getItemCount(): Int = fotosUrls.size

    // Method for classifying the image
    private fun classifyImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            showToast("Image URL is empty.")
            return
        }

        thread {
            val json = JSONObject().apply {
                put("image_url", imageUrl)
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://a7c2-2806-2f0-9181-8aa8-44b8-5c56-51ae-22b3.ngrok-free.app/clasificar") // Use your actual URL here
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FotosAdapter", "Classification error: ${e.message}")
                    showToast("Failed to classify image.")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        handleClassificationResponse(response)
                    } else {
                        Log.e("FotosAdapter", "Classification failed with code: ${response.code}")
                        showToast("Classification failed with code: ${response.code}")
                    }
                }
            })
        }
    }

    // Handle classification response
    private fun handleClassificationResponse(response: Response) {
        response.body?.string()?.let { responseBody ->
            Log.d("FotosAdapter", "Response body: $responseBody")

            val jsonResponse = JSONObject(responseBody)
            val yellowPercentage = jsonResponse.optDouble("Porcentaje de amarillo", 0.0)
            val bluePercentage = jsonResponse.optDouble("Porcentaje de azul", 0.0)
            val redPercentage = jsonResponse.optDouble("Porcentaje de rojo", 0.0)

            if (jsonResponse.has("predictions_image")) {
                val predictionsImageBase64 = jsonResponse.getString("predictions_image")
                val classificationResultsBundle = Bundle().apply {
                    putDouble("Porcentaje de amarillo", yellowPercentage)
                    putDouble("Porcentaje de azul", bluePercentage)
                    putDouble("Porcentaje de rojo", redPercentage)
                }

                Log.d("FotosAdapter", "Classification results Bundle: $classificationResultsBundle")

                val intent = Intent(context, ClassificationActivity::class.java).apply {
                    putExtra("BASE64_IMAGE", predictionsImageBase64)
                    putExtra("CLASSIFICATION_RESULTS", classificationResultsBundle)
                }
                context.startActivity(intent)
            } else {
                Log.e("FotosAdapter", "No 'predictions_image' found in response")
            }
        } ?: Log.e("FotosAdapter", "Response body is null")
    }

    // Show toast message
    private fun showToast(message: String) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // ViewHolder class for binding views
    inner class FotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenFotoItem: ImageView = itemView.findViewById(R.id.imagenFotoItem)
        val botonClasificarItem: Button = itemView.findViewById(R.id.botonClasificarItem)
    }
}
