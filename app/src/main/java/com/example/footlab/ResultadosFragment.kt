package com.example.footlab

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ResultadosFragment : Fragment() {

    private lateinit var recyclerViewFechas: RecyclerView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resultados, container, false)

        recyclerViewFechas = view.findViewById(R.id.recyclerViewFechas)
        recyclerViewFechas.layoutManager = LinearLayoutManager(context) // Usa "context" en lugar de "requireContext()"
        firestore = FirebaseFirestore.getInstance()

        cargarFechas()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Hide the BottomAppBar, BottomNavigation, and FAB when ResultadosFragment is visible
        activity?.findViewById<View>(R.id.bottomAppBar)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.fab)?.visibility = View.GONE
    }




    private fun cargarFechas() {
        val fechasList = mutableListOf<String>()

        val sharedPreferences = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE) // Usa "context" en lugar de "requireContext()"
        val username = sharedPreferences?.getString("Username", null)
        val campo = if (username?.contains("@") == true) "Email" else "UserName"

        if (username != null) {
            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        for (paciente in documentos.documents) {
                            val resultados = paciente.get("Resultados") as? ArrayList<HashMap<String, Any>>
                            resultados?.forEach { resultado ->
                                val fecha = resultado["Fecha"] as? Timestamp
                                fecha?.let {
                                    // Convierte la fecha a formato legible
                                    val fechaLegible = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(fecha.toDate())
                                    fechasList.add(fechaLegible)
                                }
                            }
                        }

                        // Configurar el RecyclerView con los datos
                        recyclerViewFechas.adapter = FechaAdapter(fechasList) { fechaSeleccionada ->
                            println("Fecha seleccionada en el adapter: $fechaSeleccionada")
                            guardarFechaYMostrarDetalle(fechaSeleccionada)
                        }
                    } else {
                        showAlert("No se encontraron resultados.")
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al recuperar el documento.")
                }
        } else {
            showAlert("Usuario no encontrado.")
        }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(context) // Usa "context" en lugar de "requireContext()"
        builder.setTitle("Mensaje")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun guardarFechaYMostrarDetalle(fechaSeleccionada: String) {
        // Guardar la fecha en memoria (por ejemplo, usando SharedPreferences)
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("FECHA_SELECCIONADA", fechaSeleccionada)?.apply()

        // Navegar al siguiente Activity
        val intent = Intent(requireContext(), ResultadosClasificadosActivity::class.java)
        startActivity(intent)
    }
}