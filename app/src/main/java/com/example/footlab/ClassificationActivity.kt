package com.example.footlab

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ClassificationActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results) // Ensure this is your correct layout file

        imageView = findViewById(R.id.classificationImageView) // Ensure this ID matches your layout

        // Get the Base64 string passed from FotosAdapter
        val base64Image = intent.getStringExtra("BASE64_IMAGE")

        base64Image?.let {
            displayBase64Image(it)
        }
    }

    private fun displayBase64Image(base64String: String) {
        try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle any errors while decoding and displaying the image
        }
    }
}
