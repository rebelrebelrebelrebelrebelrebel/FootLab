package com.example.footlab

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClassificationActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var redPercentageTextView: TextView
    private lateinit var yellowPercentageTextView: TextView
    private lateinit var bluePercentageTextView: TextView
    private lateinit var classificationResultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results) // Ensure you have a corresponding layout XML

        // Initialize UI components
        imageView = findViewById(R.id.classificationImageView) // Updated ID based on your layout
        redPercentageTextView = findViewById(R.id.porcentajeRojoTextView) // Updated ID
        yellowPercentageTextView = findViewById(R.id.porcentajeAmarilloTextView) // Updated ID
        bluePercentageTextView = findViewById(R.id.porcentajeAzulTextView) // Updated ID
        classificationResultsTextView = findViewById(R.id.classificationResultsTextView) // New ID for results header

        // Retrieve the Base64 image and classification results
        val base64Image = intent.getStringExtra("BASE64_IMAGE")
        val classificationResultsBundle = intent.getBundleExtra("CLASSIFICATION_RESULTS")

        // Decode and display the image
        base64Image?.let {
            val decodedBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        }

        // Extract classification results
        classificationResultsBundle?.let {
            val redPercentage = it.getDouble("Porcentaje de rojo", 0.0)
            val yellowPercentage = it.getDouble("Porcentaje de amarillo", 0.0)
            val bluePercentage = it.getDouble("Porcentaje de azul", 0.0)

            // Display the results in TextViews
            redPercentageTextView.text = getString(R.string.red_percentage, redPercentage)
            yellowPercentageTextView.text = getString(R.string.yellow_percentage, yellowPercentage)
            bluePercentageTextView.text = getString(R.string.blue_percentage, bluePercentage)

            // Optional: Update the header with a summary message
            classificationResultsTextView.text = getString(R.string.classification_results_summary)
        }
    }
}
