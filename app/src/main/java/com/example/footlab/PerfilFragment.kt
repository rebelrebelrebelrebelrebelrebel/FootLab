package com.example.tuapp

import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.footlab.R
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class PerfilFragment : Fragment() {

    private lateinit var textNombre: TextView
    private lateinit var textEdad: TextView
    private lateinit var textDiagnostico: TextView
    private lateinit var fotoPerfil: ImageView
    private lateinit var textFechaRegistro: TextView
    private lateinit var textMedicoTratante: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var pacientesRef: CollectionReference
    private lateinit var sharedPreferences: SharedPreferences
    private var pacienteId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializa las vistas
        textNombre = view.findViewById(R.id.textNombre)
        textEdad = view.findViewById(R.id.textEdad)
        textDiagnostico = view.findViewById(R.id.textDiagnostico)
        fotoPerfil = view.findViewById(R.id.fotoPerfil)
        textFechaRegistro = view.findViewById(R.id.textFechaRegistro)
        textMedicoTratante = view.findViewById(R.id.textMedicoTratante)

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el pacienteId desde SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        pacienteId = sharedPreferences.getString("PacienteID", null)

        // Verifica si el pacienteId está disponible
        if (pacienteId != null) {
            pacientesRef = db.collection("pacientes").document(pacienteId!!).collection("Credenciales")
        }

        // Ocultar información por defecto
        ocultarInformacion()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar la información de Firestore
        if (pacienteId != null) {
            mostrarInformacion()
        } else {
            // Manejar caso en que pacienteId es nulo (ej. usuario no ha iniciado sesión correctamente)
        }

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

    private fun mostrarInformacion() {
        // Obtén los datos de Firestore
        pacientesRef.document("Login").get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val nombre = documentSnapshot.getString("Nombre")
                    val edad = documentSnapshot.getDouble("Edad")?.toInt()
                    val fechaRegistroTimestamp = documentSnapshot.getTimestamp("Fecha de Registro")
                    val medico = documentSnapshot.getString("Médico")
                    val diagnostico = documentSnapshot.getString("Diagnóstico")

                    // Formatear la fecha de registro
                    fechaRegistroTimestamp?.let {
                        val fechaRegistroFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.toDate())
                        textFechaRegistro.text = "Fecha de Registro: $fechaRegistroFormatted"
                    } ?: run {
                        textFechaRegistro.text = "Fecha de Registro: No disponible"
                    }

                    // Asignar los valores a las vistas
                    textNombre.text = "Nombre: $nombre"
                    textEdad.text = "Edad: $edad"
                    textMedicoTratante.text = "Médico Tratante: $medico"
                    textDiagnostico.text = "Diagnóstico: $diagnostico"

                    // Mostrar la información
                    mostrarInformacionUI()
                }
            }
            .addOnFailureListener { exception ->
                // Manejar error
            }
    }

    private fun mostrarInformacionUI() {
        textNombre.visibility = View.VISIBLE
        textEdad.visibility = View.VISIBLE
        textDiagnostico.visibility = View.VISIBLE
        fotoPerfil.visibility = View.VISIBLE
        textFechaRegistro.visibility = View.VISIBLE
        textMedicoTratante.visibility = View.VISIBLE
    }
}
