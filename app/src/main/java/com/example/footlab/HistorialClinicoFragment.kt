package com.example.footlab

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

    private lateinit var tomarFotoLauncher: ActivityResultLauncher<Intent>
    private val storageRef = FirebaseStorage.getInstance().reference

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_historial_clinico, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Aquí puedes redirigir a LoginActivity o hacer algo más
        } else {
            // Usuario autenticado, seguimos con SharedPreferences para obtener PacienteID
            sharedPreferences =
                requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val pacienteId = sharedPreferences.getString("PacienteID", null)

            if (pacienteId != null) {
                Log.d("HistorialClinico", "PacienteID obtenido: $pacienteId")
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
        }


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




        // Obtener el PacienteID desde SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (pacienteId != null) {
            Log.d("HistorialClinico", "PacienteID obtenido: $pacienteId")
            // Verificar si el Historial clinico ya existe
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
        firestore.collection("Pacientes")
            .document(pacienteId)
            .collection("Historial clinico")
            .get()
            .addOnSuccessListener { result ->
                callback(result.isEmpty)
            }
            .addOnFailureListener {
                callback(true)
            }
    }

    private fun cargarHistorialClinico(pacienteId: String) {
        firestore.collection("Pacientes")
            .document(pacienteId)
            .collection("Historial clinico")
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

                    deshabilitarCampos()
                    buttonGuardar.visibility = View.INVISIBLE
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
                "ControlComorbilidades" to editTextControlComorbilidades.text.toString(),
                "FechaRegistro" to FieldValue.serverTimestamp()
            )

            mostrarDialogoFoto()

            val pacienteId = sharedPreferences.getString("PacienteID", null)
            if (pacienteId != null) {
                firestore.collection("Pacientes")
                    .document(pacienteId)
                    .collection("Historial clinico")
                    .document("Registro")
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Datos guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        deshabilitarCampos()
                        buttonGuardar.visibility = View.INVISIBLE
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Por favor, complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validarCampos(): Boolean {
        return !(editTextNombre.text.isNullOrEmpty() ||
                editTextEdad.text.isNullOrEmpty() ||
                editTextTalla.text.isNullOrEmpty() ||
                editTextPeso.text.isNullOrEmpty() ||
                editTextIMC.text.isNullOrEmpty() ||
                editTextTemperatura.text.isNullOrEmpty() ||
                editTextFrecuenciaRespiratoria.text.isNullOrEmpty() ||
                editTextFrecuenciaCardiaca.text.isNullOrEmpty() ||
                editTextTensionArterial.text.isNullOrEmpty() ||
                editTextTabaquismo.text.isNullOrEmpty() ||
                editTextAlcoholismo.text.isNullOrEmpty() ||
                editTextSedentarismo.text.isNullOrEmpty() ||
                editTextHabitosAlimenticios.text.isNullOrEmpty() ||
                editTextTipoDiabetes.text.isNullOrEmpty() ||
                editTextHipertensionArterial.text.isNullOrEmpty() ||
                editTextDislipidemia.text.isNullOrEmpty() ||
                editTextObesidad.text.isNullOrEmpty() ||
                editTextControlGlicemico.text.isNullOrEmpty() ||
                editTextManejoPieDiabetico.text.isNullOrEmpty() ||
                editTextControlComorbilidades.text.isNullOrEmpty())
    }

    private fun habilitarCampos() {
        editTextNombre.isEnabled = true
        editTextEdad.isEnabled = true
        editTextTalla.isEnabled = true
        editTextPeso.isEnabled = true
        editTextIMC.isEnabled = true
        editTextTemperatura.isEnabled = true
        editTextFrecuenciaRespiratoria.isEnabled = true
        editTextFrecuenciaCardiaca.isEnabled = true
        editTextTensionArterial.isEnabled = true
        editTextTabaquismo.isEnabled = true
        editTextAlcoholismo.isEnabled = true
        editTextSedentarismo.isEnabled = true
        editTextHabitosAlimenticios.isEnabled = true
        editTextTipoDiabetes.isEnabled = true
        editTextHipertensionArterial.isEnabled = true
        editTextDislipidemia.isEnabled = true
        editTextObesidad.isEnabled = true
        editTextControlGlicemico.isEnabled = true
        editTextManejoPieDiabetico.isEnabled = true
        editTextControlComorbilidades.isEnabled = true
        buttonGuardar.visibility = View.VISIBLE
    }

    private fun deshabilitarCampos() {
        editTextNombre.isEnabled = false
        editTextEdad.isEnabled = false
        editTextTalla.isEnabled = false
        editTextPeso.isEnabled = false
        editTextIMC.isEnabled = false
        editTextTemperatura.isEnabled = false
        editTextFrecuenciaRespiratoria.isEnabled = false
        editTextFrecuenciaCardiaca.isEnabled = false
        editTextTensionArterial.isEnabled = false
        editTextTabaquismo.isEnabled = false
        editTextAlcoholismo.isEnabled = false
        editTextSedentarismo.isEnabled = false
        editTextHabitosAlimenticios.isEnabled = false
        editTextTipoDiabetes.isEnabled = false
        editTextHipertensionArterial.isEnabled = false
        editTextDislipidemia.isEnabled = false
        editTextObesidad.isEnabled = false
        editTextControlGlicemico.isEnabled = false
        editTextManejoPieDiabetico.isEnabled = false
        editTextControlComorbilidades.isEnabled = false
        buttonGuardar.visibility = View.INVISIBLE
    }

    private fun mostrarDialogoCreacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Crear Historial Clínico")
            .setMessage("Debe crear el historial clínico para poder capturar fotografías.")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoFoto() {
        AlertDialog.Builder(requireContext())
            .setTitle("Foto")
            .setMessage("¿Deseas subir una foto?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                tomarFotoConPermiso()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun tomarFotoConPermiso() {
        // Llama a la función del Activity para pedir permiso y abrir cámara
        (activity as? MainView)?.checkPermissionsAndOpenCamera()
    }
}