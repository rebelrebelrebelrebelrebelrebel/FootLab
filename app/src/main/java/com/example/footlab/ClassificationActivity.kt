package com.example.footlab

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Date

class ClassificationActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var redPercentageTextView: TextView
    private lateinit var yellowPercentageTextView: TextView
    private lateinit var bluePercentageTextView: TextView
    private lateinit var classificationResultsTextView: TextView
    private lateinit var perimetroTextView: TextView // Perímetro
    private lateinit var areaTextView: TextView // Área
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    var red = 0.0
    var yellow = 0.0
    var blue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results)

        // Inicializa FirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        imageView = findViewById(R.id.classificationImageView)
        redPercentageTextView = findViewById(R.id.porcentajeRojoTextView)
        yellowPercentageTextView = findViewById(R.id.porcentajeAmarilloTextView)
        bluePercentageTextView = findViewById(R.id.porcentajeAzulTextView)
        classificationResultsTextView = findViewById(R.id.classificationResultsTextView)
        perimetroTextView = findViewById(R.id.perimetroTextView) // Inicializa Perímetro
        areaTextView = findViewById(R.id.areaTextView) // Inicializa Área

        // Retrieve the Base64 image and classification results
        val base64Image = intent.getStringExtra("BASE64_IMAGE")
        val classificationResultsBundle = intent.getBundleExtra("CLASSIFICATION_RESULTS")

        // Decode and display the image
        base64Image?.let {
            val decodedBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
            uploadImageToFirebase(bitmap)
        }

        // Mostrar resultados estáticos
        showStaticResults("Fecha Seleccionada")
    }

    // Función para mostrar los resultados estáticos
    private fun showStaticResults(fechaSeleccionada: String) {
        // Valores estáticos
        val porcentajeRojo = 2.13
        val porcentajeAmarillo = 0.0
        val porcentajeAzul = 97.87
        val area = 0.66
        val perimetro = 3.97

        // Mostrar los resultados en las TextViews
        redPercentageTextView.text = getString(R.string.red_percentage, porcentajeRojo)
        yellowPercentageTextView.text = getString(R.string.yellow_percentage, porcentajeAmarillo)
        bluePercentageTextView.text = getString(R.string.blue_percentage, porcentajeAzul)

        // Mostrar los valores de área y perímetro
        areaTextView.text = getString(R.string.area_de_la_herida, area) // Área
        perimetroTextView.text = getString(R.string.perimetro_de_la_herida, perimetro) // Perímetro

        // Optional: Actualizar el encabezado con un mensaje resumen
        classificationResultsTextView.text = getString(R.string.classification_results_summary)
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val storageRef = firebaseStorage.reference.child("${System.currentTimeMillis()}.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageInfoToFirestore(uri.toString(), red, yellow, blue)
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al cargar la imagen")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    private fun saveImageInfoToFirestore(imageUrl: String, r: Double, y: Double, b: Double) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"

            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val batch = firestore.batch()

                        for (paciente in documentos.documents) {
                            val docRef = paciente.reference
                            val resultados = paciente.get("Resultados") as? ArrayList<HashMap<String, Any>> ?: arrayListOf()
                            val currentDate = Timestamp(Date())
                            val newPhoto = hashMapOf("Fecha" to currentDate, "URL" to imageUrl, "PorcentajeEpitelial" to r,
                                "PorcentajeEsfacelar" to y, "PorcentajeNecrosado" to b)
                            batch.update(docRef, "Resultados", FieldValue.arrayUnion(newPhoto))
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                showAlert("Resultados guardados exitosamente")
                            }
                            .addOnFailureListener {
                                showAlert("Error al guardar el resultado")
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

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mensaje")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}

