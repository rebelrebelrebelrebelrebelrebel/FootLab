package com.example.footlab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

        // Cargar la imagen con Glide
        Glide.with(context)
            .load(url)
            .into(holder.imagenFotoItem)

        // Asignar eventos a los botones
        holder.botonClasificarItem.setOnClickListener {
            Log.d("ClasificarImageURL", "Clasificando imagen con URL: $url")
            classifyImage(url) // Usa el url directamente
        }

        holder.botonSegmentarItem.setOnClickListener {
            segmentImage(url) // Usa el url directamente
        }
    }

    override fun getItemCount(): Int = fotosUrls.size

    // Método para clasificar la imagen
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
                .url("https://1688-2806-2f0-92c0-6a47-2de3-c3c4-b737-b2b1.ngrok-free.app/clasificar")
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ClasificarError", e.message ?: "Unknown error")
                    showToast("Failed to classify image.")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        handleClassificationResponse(response)
                    } else {
                        Log.e("ClasificarError", "Response Code: ${response.code}")
                        showToast("Classification failed with code: ${response.code}")
                    }
                }
            })
        }
    }

    // Maneja la respuesta de clasificación
    private fun handleClassificationResponse(response: Response) {
        response.body?.string()?.let { responseBody ->
            val jsonResponse = JSONObject(responseBody)
            if (jsonResponse.has("predictions_image")) {
                val predictionsImageBase64 = jsonResponse.getString("predictions_image")
                Log.d("ClasificarResult", "Predictions Image Base64: $predictionsImageBase64")

                val intent = Intent(context, ClassificationActivity::class.java).apply {
                    putExtra("BASE64_IMAGE", predictionsImageBase64)
                }
                context.startActivity(intent)
            } else {
                Log.e("ClasificarError", "No se encontró 'predictions_image' en la respuesta")
            }
        }
    }

    // Método para segmentar la imagen
    private fun segmentImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            showToast("Image URL is empty.")
            return
        }

        thread {
            val bitmap = loadBitmapFromURL(imageUrl)
            bitmap?.let {
                val targetSize = Pair(224, 224)
                val (maskImage, segmentedImage) = predictMask(bitmap, targetSize)

                saveBitmapAndGetUrl(maskImage) { maskImageUrl ->
                    saveBitmapAndGetUrl(segmentedImage) { segmentedImageUrl ->
                        val intent = Intent(context, ResultsActivity::class.java).apply {
                            putExtra("MASK_IMAGE_URL", maskImageUrl)
                            putExtra("SEGMENTED_IMAGE_URL", segmentedImageUrl)
                        }
                        context.startActivity(intent)
                    }
                }
            } ?: Log.e("SegmentarError", "Failed to load image for segmentation")
        }
    }

    // Cargar Bitmap desde URL
    private fun loadBitmapFromURL(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e("LoadBitmapError", "Error loading bitmap: ${e.message}")
            null
        }
    }

    // Predecir máscara y segmentar la imagen
    private fun predictMask(bitmap: Bitmap, targetSize: Pair<Int, Int>): Pair<Bitmap, Bitmap> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize.first, targetSize.second, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(targetSize.first * targetSize.second)
        resizedBitmap.getPixels(intValues, 0, targetSize.first, 0, 0, targetSize.first, targetSize.second)
        intValues.forEach { pixelValue ->
            inputBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second).apply {
            order(ByteOrder.nativeOrder())
        }
        interpreter.run(inputBuffer, outputBuffer)

        return generateResultBitmaps(outputBuffer, resizedBitmap, targetSize)
    }

    // Generar Bitmaps de resultados
    private fun generateResultBitmaps(outputBuffer: ByteBuffer, resizedBitmap: Bitmap, targetSize: Pair<Int, Int>): Pair<Bitmap, Bitmap> {
        val maskBitmap = Bitmap.createBitmap(targetSize.first, targetSize.second, Bitmap.Config.ARGB_8888)
        val segmentedBitmap = Bitmap.createBitmap(targetSize.first, targetSize.second, Bitmap.Config.ARGB_8888)
        outputBuffer.rewind()

        for (y in 0 until targetSize.second) {
            for (x in 0 until targetSize.first) {
                val value = outputBuffer.getFloat()
                val color = if (value > 0.5f) Color.WHITE else Color.BLACK
                maskBitmap.setPixel(x, y, color)
                segmentedBitmap.setPixel(x, y, if (color == Color.WHITE) resizedBitmap.getPixel(x, y) else Color.BLACK)
            }
        }
        return Pair(maskBitmap, segmentedBitmap)
    }

    // Guardar Bitmap en Firebase Storage y obtener la URL
    private fun saveBitmapAndGetUrl(bitmap: Bitmap, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.png")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri -> callback(uri.toString()) }
                    .addOnFailureListener { callback("") }
            }
            .addOnFailureListener { callback("") }
    }

    // Mostrar un Toast
    private fun showToast(message: String) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    inner class FotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenFotoItem: ImageView = itemView.findViewById(R.id.imagenFotoItem)
        val botonClasificarItem: Button = itemView.findViewById(R.id.botonClasificarItem)
        val botonSegmentarItem: Button = itemView.findViewById(R.id.botonSegmentarItem)
    }
}
