package com.example.footlab

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ResultadosClasificadosActivity : AppCompatActivity() {

    private lateinit var classificationImageView: ImageView
    private lateinit var classificationResultsTextView: TextView
    private lateinit var porcentajeRojoTextView: TextView
    private lateinit var porcentajeAmarilloTextView: TextView
    private lateinit var porcentajeAzulTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var areaTextView: TextView // Área
    private lateinit var perimetroTextView: TextView // Perímetro
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_clasificados)

        classificationImageView = findViewById(R.id.classificationImageView)
        classificationResultsTextView = findViewById(R.id.classificationResultsTextView)
        porcentajeRojoTextView = findViewById(R.id.porcentajeRojoTextView)
        porcentajeAmarilloTextView = findViewById(R.id.porcentajeAmarilloTextView)
        porcentajeAzulTextView = findViewById(R.id.porcentajeAzulTextView)
        titleTextView = findViewById(R.id.titleTextView)
        areaTextView = findViewById(R.id.areaTextView) // Inicializa Área
        perimetroTextView = findViewById(R.id.perimetroTextView) // Inicializa Perímetro

        firestore = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val fecha11 = sharedPreferences.getString("FECHA_SELECCIONADA", null)

        if (fecha11 != null){
            // Se muestran valores estáticos
            showStaticResults(fecha11)
        } else {
            showAlert("No se encontró la fecha seleccionada. $fecha11")
        }
    }

    private fun showStaticResults(fechaSeleccionada: String) {
        // Valores estáticos
        val porcentajeRojo = 2.13
        val porcentajeAmarillo = 0.0
        val porcentajeAzul = 97.87
        val area = 0.66
        val perimetro = 3.97

        // Configura los valores estáticos en los TextViews
        porcentajeRojoTextView.text = getString(R.string.porcentaje_epitelial_rojo2, porcentajeRojo)
        porcentajeAmarilloTextView.text = getString(R.string.porcentaje_esfacelar_amarillo2, porcentajeAmarillo)
        porcentajeAzulTextView.text = getString(R.string.porcentaje_necrosado_azul2, porcentajeAzul)
        areaTextView.text = getString(R.string.area_de_la_herida, area)
        perimetroTextView.text = getString(R.string.perimetro_de_la_herida, perimetro)

        // También muestra la fecha seleccionada, si se desea
        titleTextView.text = fechaSeleccionada
    }

    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
