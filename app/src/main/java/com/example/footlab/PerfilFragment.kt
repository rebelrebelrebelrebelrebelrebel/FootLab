package com.example.tuapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.footlab.R

class PerfilFragment : Fragment() {

    private lateinit var textNombre: TextView
    private lateinit var textEdad: TextView
    private lateinit var textDiagnostico: TextView
    private lateinit var fotoPerfil: ImageView
    private lateinit var textFechaRegistro: TextView
    private lateinit var textMedicoTratante: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        textNombre = view.findViewById(R.id.textNombre)
        textEdad = view.findViewById(R.id.textEdad)
        textDiagnostico = view.findViewById(R.id.textDiagnostico)
        fotoPerfil = view.findViewById(R.id.fotoPerfil)
        textFechaRegistro = view.findViewById(R.id.textFechaRegistro)
        textMedicoTratante = view.findViewById(R.id.textMedicoTratante)

        textNombre.text = "Nombre: Regina Ríos López"
        textEdad.text = "Edad: 28"
        textDiagnostico.text = "Diagnóstico: Pendiente de actualización"
        textFechaRegistro.text = "Fecha de Registro: 01/08/2024"
        textMedicoTratante.text = "Médico Tratante: Dr. Pérez"

        ocultarInformacion()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarInformacion()

        // Interceptar el evento de "Back" desde el fragmento
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // No hacer nada cuando se presiona "Atrás"
            // Esto evita que la actividad o fragmento realice la acción predeterminada
        }
    }

    private fun ocultarInformacion() {
        textNombre.visibility = View.GONE
        textEdad.visibility = View.GONE
        textDiagnostico.visibility = View.GONE
        fotoPerfil.visibility = View.GONE
        textFechaRegistro.visibility = View.GONE
        textMedicoTratante.visibility = View.GONE
    }

    fun mostrarInformacion() {
        textNombre.visibility = View.VISIBLE
        textEdad.visibility = View.VISIBLE
        textDiagnostico.visibility = View.VISIBLE
        fotoPerfil.visibility = View.VISIBLE
        textFechaRegistro.visibility = View.VISIBLE
        textMedicoTratante.visibility = View.VISIBLE
    }
}
