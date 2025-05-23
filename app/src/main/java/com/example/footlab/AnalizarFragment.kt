package com.example.footlab

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
    // private lateinit var interpreter: Interpreter  <-- eliminar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_galeria, container, false)

        recyclerViewFotos = rootView.findViewById(R.id.recycler_view_fotos)
        recyclerViewFotos.layoutManager = LinearLayoutManager(context)

        FirebaseUtils.cargarFotos(requireContext()) { fotos ->
            if (fotos.isNotEmpty()) {
                // Aquí ya sin interpreter
                fotosAdapter = FotosAdapter(requireContext(), fotos) { imageUrl ->
                    Toast.makeText(context, "Foto seleccionada: $imageUrl", Toast.LENGTH_SHORT).show()
                }
                recyclerViewFotos.adapter = fotosAdapter
            } else {
                Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }
}
