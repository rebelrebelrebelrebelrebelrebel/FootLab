package com.example.footlab

import HistorialClinicoFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import openFragment

class HistorialFragment : Fragment() {

    private lateinit var botonHistClinica: Button
    private lateinit var botonResultados: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        botonHistClinica = view.findViewById(R.id.botonVerHistorialClinico)
        botonResultados = view.findViewById(R.id.botonVerResultados)


        botonHistClinica.setOnClickListener {
            requireActivity().openFragment(HistorialClinicoFragment(), "HistorialClinicoFragmentTag")
        }


        botonResultados.setOnClickListener {
            requireActivity().openFragment(ResultadosFragment(), "ResultadosFragmentTag")
        }

        return view
    }

}
