package com.example.footlab

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialClinicoFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextNombre: EditText
    private lateinit var editTextEdad: EditText
    private lateinit var editTextTalla: EditText
    private lateinit var editTextPeso: EditText
    private lateinit var editTextIMC: EditText
    private lateinit var editTextTemperatura: EditText
    private lateinit var editTextFrecuenciaRespiratoria: EditText
    private lateinit var editTextFrecuenciaCardiaca: EditText
    private lateinit var editTextTensionArterial: EditText
    private lateinit var editTextTabaquismo: EditText
    private lateinit var editTextAlcoholismo: EditText
    private lateinit var editTextSedentarismo: EditText
    private lateinit var editTextHabitosAlimenticios: EditText
    private lateinit var editTextTipoDiabetes: EditText
    private lateinit var editTextHipertensionArterial: EditText
    private lateinit var editTextDislipidemia: EditText
    private lateinit var editTextObesidad: EditText
    private lateinit var editTextControlGlicemico: EditText
    private lateinit var editTextManejoPieDiabetico: EditText
    private lateinit var editTextControlComorbilidades: EditText
    private lateinit var buttonGuardar: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_historial_clinico, container, false)

        // Inicializar vistas
        editTextNombre = view.findViewById(R.id.editTextNombres)
        editTextEdad = view.findViewById(R.id.editTextEdad)
        editTextTalla = view.findViewById(R.id.editTextTalla)
        editTextPeso = view.findViewById(R.id.editTextPeso)
        editTextIMC = view.findViewById(R.id.editTextIMC)
        editTextTemperatura = view.findViewById(R.id.editTextTemperatura)
        editTextFrecuenciaRespiratoria = view.findViewById(R.id.editTextFrecuenciaRespiratoria)
        editTextFrecuenciaCardiaca = view.findViewById(R.id.editTextFrecuenciaCardiaca)
        editTextTensionArterial = view.findViewById(R.id.editTextTensionArterial)
        editTextTabaquismo = view.findViewById(R.id.editTextTabaquismo)
        editTextAlcoholismo = view.findViewById(R.id.editTextAlcoholismo)
        editTextSedentarismo = view.findViewById(R.id.editTextSedentarismo)
        editTextHabitosAlimenticios = view.findViewById(R.id.editTextHabitosAlimenticios)
        editTextTipoDiabetes = view.findViewById(R.id.editTextTipoDiabetes)
        editTextHipertensionArterial = view.findViewById(R.id.editTextHipertensionArterial)
        editTextDislipidemia = view.findViewById(R.id.editTextDislipidemia)
        editTextObesidad = view.findViewById(R.id.editTextObesidad)
        editTextControlGlicemico = view.findViewById(R.id.editTextControlGlicemico)
        editTextManejoPieDiabetico = view.findViewById(R.id.editTextManejoPieDiabetico)
        editTextControlComorbilidades = view.findViewById(R.id.editTextControlComorbilidades)
        buttonGuardar = view.findViewById(R.id.buttonGuardar)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Obtener el PacienteID desde SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (pacienteId != null) {
            Log.d("HistorialClinico", "PacienteID obtenido: $pacienteId")
            // Verificar si el historial clínico ya existe
            isHistorialClinicoVacio(pacienteId) { vacio ->
                if (vacio) {
                    habilitarCampos()
                    mostrarDialogoCreacion()
                } else {
                    cargarHistorialClinico(pacienteId)
                    deshabilitarCampos()
                }
            }
        } else {
            Log.e("HistorialClinico", "No se encontró el PacienteID en SharedPreferences")
            Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        buttonGuardar.setOnClickListener {
            guardarHistorialClinico()
        }

        return view
    }

    private fun isHistorialClinicoVacio(pacienteId: String, callback: (Boolean) -> Unit) {
        firestore.collection("pacientes")
            .document(pacienteId)
            .collection("Historial clínico")
            .get()
            .addOnSuccessListener { result ->
                callback(result.isEmpty)
            }
            .addOnFailureListener {
                callback(true)
            }
    }

    private fun cargarHistorialClinico(pacienteId: String) {
        firestore.collection("pacientes")
            .document(pacienteId)
            .collection("Historial clínico")
            .document("Registro")
            .get()
            .addOnSuccessListener { document ->
                document?.let {
                    editTextNombre.setText(it.getString("Nombre"))
                    editTextEdad.setText(it.getString("Edad"))
                    editTextTalla.setText(it.getString("Talla"))
                    editTextPeso.setText(it.getString("Peso"))
                    editTextIMC.setText(it.getString("IMC"))
                    editTextTemperatura.setText(it.getString("Temperatura"))
                    editTextFrecuenciaRespiratoria.setText(it.getString("FrecuenciaRespiratoria"))
                    editTextFrecuenciaCardiaca.setText(it.getString("FrecuenciaCardiaca"))
                    editTextTensionArterial.setText(it.getString("TensionArterial"))
                    editTextTabaquismo.setText(it.getString("Tabaquismo"))
                    editTextAlcoholismo.setText(it.getString("Alcoholismo"))
                    editTextSedentarismo.setText(it.getString("Sedentarismo"))
                    editTextHabitosAlimenticios.setText(it.getString("HabitosAlimenticios"))
                    editTextTipoDiabetes.setText(it.getString("TipoDiabetes"))
                    editTextHipertensionArterial.setText(it.getString("HipertensionArterial"))
                    editTextDislipidemia.setText(it.getString("Dislipidemia"))
                    editTextObesidad.setText(it.getString("Obesidad"))
                    editTextControlGlicemico.setText(it.getString("ControlGlicemico"))
                    editTextManejoPieDiabetico.setText(it.getString("ManejoPieDiabetico"))
                    editTextControlComorbilidades.setText(it.getString("ControlComorbilidades"))
                }
            }
    }

    private fun guardarHistorialClinico() {
        if (validarCampos()) {
            val datos = hashMapOf(
                "Nombre" to editTextNombre.text.toString(),
                "Edad" to editTextEdad.text.toString(),
                "Talla" to editTextTalla.text.toString(),
                "Peso" to editTextPeso.text.toString(),
                "IMC" to editTextIMC.text.toString(),
                "Temperatura" to editTextTemperatura.text.toString(),
                "FrecuenciaRespiratoria" to editTextFrecuenciaRespiratoria.text.toString(),
                "FrecuenciaCardiaca" to editTextFrecuenciaCardiaca.text.toString(),
                "TensionArterial" to editTextTensionArterial.text.toString(),
                "Tabaquismo" to editTextTabaquismo.text.toString(),
                "Alcoholismo" to editTextAlcoholismo.text.toString(),
                "Sedentarismo" to editTextSedentarismo.text.toString(),
                "HabitosAlimenticios" to editTextHabitosAlimenticios.text.toString(),
                "TipoDiabetes" to editTextTipoDiabetes.text.toString(),
                "HipertensionArterial" to editTextHipertensionArterial.text.toString(),
                "Dislipidemia" to editTextDislipidemia.text.toString(),
                "Obesidad" to editTextObesidad.text.toString(),
                "ControlGlicemico" to editTextControlGlicemico.text.toString(),
                "ManejoPieDiabetico" to editTextManejoPieDiabetico.text.toString(),
                "ControlComorbilidades" to editTextControlComorbilidades.text.toString()
            )

            val pacienteId = sharedPreferences.getString("PacienteID", null)

            if (pacienteId != null) {
                firestore.collection("pacientes")
                    .document(pacienteId)
                    .collection("Historial clínico")
                    .document("Registro")
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Historial guardado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        deshabilitarCampos()
                        mostrarDialogoFoto()
                        buttonGuardar.visibility = View.GONE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        return editTextNombre.text.isNotBlank() &&
                editTextEdad.text.isNotBlank() &&
                editTextTalla.text.isNotBlank() &&
                editTextPeso.text.isNotBlank() &&
                editTextIMC.text.isNotBlank() &&
                editTextTemperatura.text.isNotBlank() &&
                editTextFrecuenciaRespiratoria.text.isNotBlank() &&
                editTextFrecuenciaCardiaca.text.isNotBlank() &&
                editTextTensionArterial.text.isNotBlank() &&
                editTextTabaquismo.text.isNotBlank() &&
                editTextAlcoholismo.text.isNotBlank() &&
                editTextSedentarismo.text.isNotBlank() &&
                editTextHabitosAlimenticios.text.isNotBlank() &&
                editTextTipoDiabetes.text.isNotBlank() &&
                editTextHipertensionArterial.text.isNotBlank() &&
                editTextDislipidemia.text.isNotBlank() &&
                editTextObesidad.text.isNotBlank() &&
                editTextControlGlicemico.text.isNotBlank() &&
                editTextManejoPieDiabetico.text.isNotBlank() &&
                editTextControlComorbilidades.text.isNotBlank()
    }

    private fun mostrarDialogoCreacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Crear Historial Clínico")
            .setMessage("No se encontró un historial. ¿Deseas crear uno?")
            .setPositiveButton("Crear") { _, _ ->
                Toast.makeText(requireContext(), "Completa los datos para guardar", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun mostrarDialogoFoto() {
        AlertDialog.Builder(requireContext())
            .setTitle("Foto de Perfil")
            .setMessage("¿Deseas subir una foto de perfil?")
            .setPositiveButton("Subir Foto") { _, _ ->
                Toast.makeText(requireContext(), "Funcionalidad pendiente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun habilitarCampos() {
        listOf(
            editTextNombre, editTextEdad, editTextTalla, editTextPeso, editTextIMC,
            editTextTemperatura, editTextFrecuenciaRespiratoria, editTextFrecuenciaCardiaca,
            editTextTensionArterial, editTextTabaquismo, editTextAlcoholismo, editTextSedentarismo,
            editTextHabitosAlimenticios, editTextTipoDiabetes, editTextHipertensionArterial,
            editTextDislipidemia, editTextObesidad, editTextControlGlicemico,
            editTextManejoPieDiabetico, editTextControlComorbilidades
        ).forEach { it.isEnabled = true }
    }

    private fun deshabilitarCampos() {
        listOf(
            editTextNombre, editTextEdad, editTextTalla, editTextPeso, editTextIMC,
            editTextTemperatura, editTextFrecuenciaRespiratoria, editTextFrecuenciaCardiaca,
            editTextTensionArterial, editTextTabaquismo, editTextAlcoholismo, editTextSedentarismo,
            editTextHabitosAlimenticios, editTextTipoDiabetes, editTextHipertensionArterial,
            editTextDislipidemia, editTextObesidad, editTextControlGlicemico,
            editTextManejoPieDiabetico, editTextControlComorbilidades
        ).forEach { it.isEnabled = false }
    }

}
