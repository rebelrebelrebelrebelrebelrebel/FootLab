package com.example.footlab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.footlab.utils.FirebaseUtils

class GaleriaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_galeria, container, false)

        val sharedPreferences = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        username?.let {
            // Cargar las fotos desde Firebase solo si no se han cargado aún
            FirebaseUtils.cargarFotos(requireContext(), it) { fotos ->
                if (fotos.isNotEmpty()) {
                    // Si hay fotos, mostrar un mensaje de éxito
                    Toast.makeText(context, "Fotos cargadas exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    // Si no hay fotos, mostrar un mensaje correspondiente
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }
}
