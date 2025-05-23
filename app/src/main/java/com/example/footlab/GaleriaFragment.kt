package com.example.footlab

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

        FirebaseUtils.cargarFotos(requireContext()) { fotos ->
            if (fotos.isNotEmpty()) {
                Toast.makeText(context, "Fotos cargadas exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }
}

