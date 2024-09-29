package com.example.footlab

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
import java.io.BufferedInputStream
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
    private val onClasificarClick: (String) -> Unit // Callback para el botón de clasificar
) : RecyclerView.Adapter<FotosAdapter.FotosViewHolder>() {

    private var currentImageUrl: String? = null // Variable para guardar la URL de la imagen

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false)
        return FotosViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotosViewHolder, position: Int) {
        val url = fotosUrls[position]
        Glide.with(context)
            .load(url)
            .into(holder.imagenFotoItem)

        // Save the URL for later use
        currentImageUrl = url

        // Listener para el botón de clasificar
        holder.botonClasificarItem.setOnClickListener {
            Log.d("ClasificarClick", "Fetching image URL...")

            // Fetch the image from Firebase Storage in a background thread
            thread {
                val bitmap = loadBitmapFromURL(currentImageUrl ?: "")
                if (bitmap != null) {
                    currentImageUrl?.let { url ->
                        // Create a JSON object with the correct key
                        val json = JSONObject()
                        json.put("image_url", url) // Use "image_url" instead of "imageUrl"
                        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                        // Make the POST request to the Flask server
                        val request = Request.Builder()
                            .url("http://192.168.100.176:5000/clasificar")
                            .post(requestBody)
                            .build()

                        // Use OkHttpClient for the request
                        val client = OkHttpClient()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e("ClasificarError", e.message ?: "Unknown error")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    // Handle the successful response here
                                } else {
                                    Log.e("ClasificarError", "Response Code: ${response.code}")
                                }
                            }
                        })
                    }
                } else {
                    Log.e("ClasificarClick", "Failed to load image from URL")
                }
            }
        }

        holder.botonSegmentarItem.setOnClickListener {
            Log.d("SegmentarClick", "URL: $currentImageUrl")  // Log the URL for the Segmentar button
            thread {
                currentImageUrl?.let { url ->
                    val bitmap = loadBitmapFromURL(url) // Load original image
                    bitmap?.let {
                        val targetSize = Pair(224, 224)
                        val (maskImage, segmentedImage) = predictMask(bitmap, targetSize)

                        saveBitmapAndGetUrl(maskImage) { maskImageUrl ->
                            saveBitmapAndGetUrl(segmentedImage) { segmentedImageUrl ->
                                val intent = Intent(context, ResultsActivity::class.java)
                                intent.putExtra("MASK_IMAGE_URL", maskImageUrl)
                                intent.putExtra("SEGMENTED_IMAGE_URL", segmentedImageUrl)
                                context.startActivity(intent)

                                // Use the same currentImageUrl for classification
                                currentImageUrl?.let { onClasificarClick(it) } // Pass the original image URL to classify
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = fotosUrls.size

    private fun loadBitmapFromURL(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(BufferedInputStream(connection.inputStream))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun predictMask(bitmap: Bitmap, targetSize: Pair<Int, Int>): Pair<Bitmap, Bitmap> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize.first, targetSize.second, true)

        val inputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(targetSize.first * targetSize.second)
        resizedBitmap.getPixels(intValues, 0, targetSize.first, 0, 0, targetSize.first, targetSize.second)
        intValues.forEachIndexed { index, pixelValue ->
            inputBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second)
        outputBuffer.order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)

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

    private fun saveBitmapAndGetUrl(bitmap: Bitmap, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.png")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { uri -> callback(uri.toString()) }
                    .addOnFailureListener { callback("") } // Handle download URL error
            }
            .addOnFailureListener { callback("") } // Handle image upload error
    }

    class FotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenFotoItem: ImageView = itemView.findViewById(R.id.imagenFotoItem)
        val botonClasificarItem: Button = itemView.findViewById(R.id.botonClasificarItem)
        val botonSegmentarItem: Button = itemView.findViewById(R.id.botonSegmentarItem)
    }
}
