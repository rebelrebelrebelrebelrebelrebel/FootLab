package com.example.footlab

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClassificationActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var resultsTextView: TextView
    private lateinit var porcentajeRojoTextView: TextView
    private lateinit var porcentajeAmarilloTextView: TextView
    private lateinit var porcentajeAzulTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results) // Asegúrate de que este sea tu archivo de diseño correcto

        // Inicializar las vistas
        imageView = findViewById(R.id.classificationImageView)
        resultsTextView = findViewById(R.id.classificationResultsTextView)
        porcentajeRojoTextView = findViewById(R.id.porcentajeRojoTextView)
        porcentajeAmarilloTextView = findViewById(R.id.porcentajeAmarilloTextView)
        porcentajeAzulTextView = findViewById(R.id.porcentajeAzulTextView)

        // Obtener la cadena Base64 y resultados de clasificación pasados desde FotosAdapter
        val base64Image = intent.getStringExtra("BASE64_IMAGE")
        val classificationResults = intent.getSerializableExtra("CLASSIFICATION_RESULTS") as? HashMap<String, Double>

        // Logs para verificar la recepción de datos
        Log.d("ClassificationActivity", "Base64 Image: $base64Image")
        Log.d("ClassificationActivity", "Resultados de Clasificación: $classificationResults")

        base64Image?.let { displayBase64Image(it) }
        classificationResults?.let { displayClassificationResults(it) }
            ?: Log.e("ClassificationActivity", "Los resultados de clasificación son nulos")
    }

    private fun displayBase64Image(base64String: String) {
        try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("ClassificationActivity", "Error al decodificar y mostrar la imagen: ${e.message}", e)
        }
    }

    private fun displayClassificationResults(results: HashMap<String, Double>) {
        // Actualizar directamente los TextViews con los resultados
        porcentajeRojoTextView.text = "Porcentaje de Rojo: ${results["Porcentaje de rojo"] ?: 0.0}%"
        porcentajeAmarilloTextView.text = "Porcentaje de Amarillo: ${results["Porcentaje de amarillo"] ?: 0.0}%"
        porcentajeAzulTextView.text = "Porcentaje de Azul: ${results["Porcentaje de azul"] ?: 0.0}%"

        // Log para verificar que los resultados se están mostrando en los TextViews
        Log.d("ClassificationActivity", "Resultados mostrados: ${results.values.joinToString()}")
    }

    // Manejar la acción del botón de retroceso en el Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Volver a la actividad anterior
        return true
    }
}
