package com.example.footlab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import openFragment

class HistorialFragment : Fragment() {

    private lateinit var botonResultados: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        botonResultados = view.findViewById(R.id.botonVerResultados)


        botonResultados.setOnClickListener {
            requireActivity().openFragment(ResultadosFragment(), "ResultadosFragmentTag")
        }

        return view
    }

}
