package com.example.footlab.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    fun cargarFotos(context: Context, callback: (List<String>) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (pacienteId == null) {
            Toast.makeText(context, "Paciente no encontrado", Toast.LENGTH_SHORT).show()
            callback(emptyList())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("Pacientes")
            .document(pacienteId)
            .collection("Imagenes")
            .get()
            .addOnSuccessListener { imagesSnapshot ->
                if (imagesSnapshot.documents.isNotEmpty()) {
                    val fotosUrls = imagesSnapshot.documents.mapNotNull { doc ->
                        doc.getString("URL")
                    }
                    callback(fotosUrls)
                } else {
                    Toast.makeText(context, "No hay fotos en Imagenes", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
    }
}
