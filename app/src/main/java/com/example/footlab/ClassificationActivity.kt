package com.example.footlab

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import java.util.Date
import java.io.ByteArrayOutputStream

class ClassificationActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var redPercentageTextView: TextView
    private lateinit var yellowPercentageTextView: TextView
    private lateinit var bluePercentageTextView: TextView
    private lateinit var classificationResultsTextView: TextView
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    var red=0.0
    var yellow=0.0
    var blue=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results) // Ensure you have a corresponding layout XML

        // Inicializa FirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
            uploadImageToFirebase(bitmap)
        }

        // Extract classification results
        classificationResultsBundle?.let {
            val redPercentage = it.getDouble("Porcentaje de rojo", 0.0)
            val yellowPercentage = it.getDouble("Porcentaje de amarillo", 0.0)
            val bluePercentage = it.getDouble("Porcentaje de azul", 0.0)

            red= redPercentage
            yellow=yellowPercentage
            blue=bluePercentage

            // Display the results in TextViews
            redPercentageTextView.text = getString(R.string.red_percentage, redPercentage)
            yellowPercentageTextView.text = getString(R.string.yellow_percentage, yellowPercentage)
            bluePercentageTextView.text = getString(R.string.blue_percentage, bluePercentage)

            // Optional: Update the header with a summary message
            classificationResultsTextView.text = getString(R.string.classification_results_summary)
        }
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
                        saveImageInfoToFirestore(uri.toString(),red,yellow,blue)
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al cargar la imagen")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    private fun saveImageInfoToFirestore(imageUrl: String,r:Double,y:Double,b:Double) {
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
