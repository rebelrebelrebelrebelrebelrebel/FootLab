package com.example.footlab

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ResultadosClasificadosActivity : AppCompatActivity() {

    private lateinit var classificationImageView: ImageView
    private lateinit var classificationResultsTextView: TextView
    private lateinit var porcentajeRojoTextView: TextView
    private lateinit var porcentajeAmarilloTextView: TextView
    private lateinit var porcentajeAzulTextView: TextView
    private lateinit var titleTextView: TextView
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

        firestore = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val fecha11 = sharedPreferences.getString("FECHA_SELECCIONADA", null)

        if (fecha11 != null){
            loadDataFromFirestore(fecha11)
        }else{
            showAlert("No se encontró la fecha seleccionada. $fecha11")
        }

    }

    private fun loadDataFromFirestore(fechaSeleccionada: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"

            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        for (paciente in documentos.documents) {
                            val resultados = paciente.get("Resultados") as? ArrayList<HashMap<String, Any>> ?: continue
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                            // Convertir la cadena a Date
                            val fechaDate: Date? = try {
                                inputFormat.parse(fechaSeleccionada)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                                null
                            }
                            val fechaSeleccionadaFormatted = fechaDate?.toCustomFormattedString()
                            var StringDate=""

                            val resultadoFiltrado = resultados.firstOrNull { resultado ->
                                val fechaFirestore = resultado["Fecha"]
                                val fechaFirestoreFormatted = when (fechaFirestore) {
                                    is Timestamp -> {
                                        // Convierte el Timestamp a Date y luego a String formateada
                                        val fechaDate=fechaFirestore.toDate()
                                        val fechaString=fechaDate.toString()
                                        StringDate=fechaString
                                    }

                                    else -> {

                                    }
                                }
                                // Compara directamente
                                StringDate == fechaSeleccionadaFormatted
                            }

                            if (resultadoFiltrado != null) {
                                // Extrae los datos
                                val imageUrl = resultadoFiltrado["URL"] as? String
                                val porcentajeRojo = resultadoFiltrado["PorcentajeEpitelial"] as? Double ?: 0.0
                                val porcentajeAmarillo = resultadoFiltrado["PorcentajeEsfacelar"] as? Double ?: 0.0
                                val porcentajeAzul = resultadoFiltrado["PorcentajeNecrosado"] as? Double ?: 0.0

                                // Configura el ImageView y los TextView
                                if (imageUrl != null) {
                                    Glide.with(this).load(imageUrl).into(classificationImageView)
                                }

                                // Muestra los porcentajes en los TextView
                                porcentajeRojoTextView.text = getString(R.string.porcentaje_epitelial_rojo2, porcentajeRojo)
                                porcentajeAmarilloTextView.text = getString(R.string.porcentaje_esfacelar_amarillo2, porcentajeAmarillo)
                                porcentajeAzulTextView.text = getString(R.string.porcentaje_necrosado_azul2, porcentajeAzul)

                                // Muestra la fecha seleccionada
                                titleTextView.text = fechaSeleccionada
                            } else {
                                showAlert("No se encontraron resultados para la fecha seleccionada.")
                            }
                        }
                    } else {
                        showAlert("Usuario no encontrado")
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al recuperar el documento")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }





    fun Date.toCustomFormattedString(): String {
        val outputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale("en", "US"))
        return outputFormat.format(this)
    }


    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}