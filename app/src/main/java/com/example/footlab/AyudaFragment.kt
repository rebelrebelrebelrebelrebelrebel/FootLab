package com.example.footlab

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class AyudaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ayuda, container, false)

        // Asumiendo que los TextView en fragment_ayuda.xml tienen los IDs textViewStep3 y textViewStep4
        val textViewStep1: TextView = view.findViewById(R.id.textViewStep1)
        val textViewStep3: TextView = view.findViewById(R.id.textViewStep3)
        val textViewStep4: TextView = view.findViewById(R.id.textViewStep4)

        // Texto con formato HTML
        val step1Text = "1. Crea un historial clínico en la sección de <b> Historial </b>."
        val step3Text = "3. Ve a la galería y selecciona el botón <b>Analizar</b> para procesar la imagen."
        val step4Text = "4. La imagen clasificada se guardará en la sección de <b>Resultados</b> de tu historial para que la puedas consultar más tarde."

        // Asignar el texto a los TextView usando Html.fromHtml
        textViewStep1.text = Html.fromHtml(step1Text, Html.FROM_HTML_MODE_LEGACY)
        textViewStep3.text = Html.fromHtml(step3Text, Html.FROM_HTML_MODE_LEGACY)
        textViewStep4.text = Html.fromHtml(step4Text, Html.FROM_HTML_MODE_LEGACY)

        return view
    }
}
