package com.example.footlab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footlab.utils.FirebaseUtils
import org.tensorflow.lite.Interpreter

class AnalizarFragment : Fragment() {

    private lateinit var recyclerViewFotos: RecyclerView
    private lateinit var fotosAdapter: FotosAdapter
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_galeria, container, false)

        // Inicializar el modelo TensorFlow Lite
        val modeloTFLite = ModeloTFLite(requireContext())
        interpreter = Interpreter(modeloTFLite.getModel())

        recyclerViewFotos = rootView.findViewById(R.id.recycler_view_fotos)
        recyclerViewFotos.layoutManager = LinearLayoutManager(context)

        val sharedPreferences = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        username?.let {
            FirebaseUtils.cargarFotos(requireContext(), it) { fotos ->
                if (fotos.isNotEmpty()) {
                    // Asignar las fotos al adaptador sin interacción con la API
                    fotosAdapter = FotosAdapter(requireContext(), fotos, interpreter) { imageUrl ->
                        // Aquí puedes manejar cualquier otra lógica que desees al hacer clic
                        Toast.makeText(context, "Foto seleccionada: $imageUrl", Toast.LENGTH_SHORT).show()
                    }
                    recyclerViewFotos.adapter = fotosAdapter
                } else {
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }
}