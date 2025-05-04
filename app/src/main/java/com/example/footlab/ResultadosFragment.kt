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
import org.json.JSONObject
import java.io.File
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
        recyclerViewFechas.layoutManager = LinearLayoutManager(context)
        firestore = FirebaseFirestore.getInstance()

        cargarFechas()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Ocultar elementos de navegación si es necesario
        activity?.findViewById<View>(R.id.bottomAppBar)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.fab)?.visibility = View.GONE
    }

    private fun cargarFechas() {
        val fechasList = mutableListOf<Pair<String, HashMap<String, Any>>>()

        val sharedPreferences = context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
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
                                    val fechaLegible = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(fecha.toDate())
                                    fechasList.add(Pair(fechaLegible, resultado))
                                }
                            }
                        }

                        recyclerViewFechas.adapter = FechaAdapter(fechasList) { fechaSeleccionada, resultadoSeleccionado ->
                            guardarFechaYMostrarDetalle(fechaSeleccionada, resultadoSeleccionado)
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

    private fun guardarFechaYMostrarDetalle(fechaSeleccionada: String, resultadoSeleccionado: HashMap<String, Any>) {
        // Convertir el HashMap a JSON string
        val json = JSONObject(resultadoSeleccionado as Map<*, *>).toString()

        // Guardar el JSON como archivo temporal
        val fileName = "resultado_temp.json"
        val file = File(requireContext().cacheDir, fileName)
        try {
            file.writeText(json)

            // Lanzar la actividad y pasar solo el nombre del archivo
            val intent = Intent(requireContext(), ResultadosClasificadosActivity::class.java)
            intent.putExtra("RESULTADO_FILE", fileName)
            startActivity(intent)
        } catch (e: Exception) {
            showAlert("Error al guardar los resultados: ${e.message}")
        }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Mensaje")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}
