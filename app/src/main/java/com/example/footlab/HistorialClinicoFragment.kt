
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.footlab.AnalizarFragment
//import com.example.footlab.AnalizarFragment
import com.example.footlab.MainView
import com.example.footlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HistorialClinicoFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos: EditText
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

    private val REQUEST_CODE_CAMERA = 1001 // O cualquier valor único que prefieras


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial_clinico, container, false)

        // Inicialización de los EditTexts
        editTextNombres = view.findViewById(R.id.editTextNombres)
        editTextApellidos = view.findViewById(R.id.editTextApellidos)
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

        val sharedPreferences2 = requireContext().getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences2.getString("Username", null)

        if (username != null) {
            val isEmpty = isHistorialClinicoVacio(username)
            if (isEmpty) {
                mostrarDialogoCreacion()
            } else {
                cargarHistorialClinico(username)
            }
        } else {
            Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        buttonGuardar.setOnClickListener {
            if (validarCampos()) {
                val datos = hashMapOf(
                    "Nombres" to editTextNombres.text.toString(),
                    "Apellidos" to editTextApellidos.text.toString(),
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
                )
                if (username != null) {
                    firestore.collection("Pacientes").document(username)
                        .set(datos)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Historial guardado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Error al guardar el historial: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun validarCampos(): Boolean {
        return editTextNombres.text.isNotEmpty() &&
                editTextApellidos.text.isNotEmpty() &&
                editTextEdad.text.isNotEmpty() &&
                editTextTalla.text.isNotEmpty() &&
                editTextPeso.text.isNotEmpty() &&
                editTextIMC.text.isNotEmpty() &&
                editTextTemperatura.text.isNotEmpty() &&
                editTextFrecuenciaRespiratoria.text.isNotEmpty() &&
                editTextFrecuenciaCardiaca.text.isNotEmpty() &&
                editTextTensionArterial.text.isNotEmpty() &&
                editTextTabaquismo.text.isNotEmpty() &&
                editTextAlcoholismo.text.isNotEmpty() &&
                editTextSedentarismo.text.isNotEmpty() &&
                editTextHabitosAlimenticios.text.isNotEmpty() &&
                editTextTipoDiabetes.text.isNotEmpty() &&
                editTextHipertensionArterial.text.isNotEmpty() &&
                editTextDislipidemia.text.isNotEmpty() &&
                editTextObesidad.text.isNotEmpty() &&
                editTextControlGlicemico.text.isNotEmpty() &&
                editTextManejoPieDiabetico.text.isNotEmpty() &&
                editTextControlComorbilidades.text.isNotEmpty()
    }

    private fun isHistorialClinicoVacio(username: String): Boolean {
        val campo = if (username.contains("@")) "Email" else "UserName"
        val docRef = firestore.collection("Pacientes").whereEqualTo(campo, username).get()
        var isEmpty = false

        docRef.addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                isEmpty = true
            }
        }
        return isEmpty
    }

    private fun mostrarDialogoCreacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Crear Historial Clínico")
            .setMessage("No se encontró un historial clínico. ¿Deseas crear uno?")
            .setPositiveButton("Crear") { _, _ ->
                Toast.makeText(requireContext(), "Crea tu historial clínico", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                requireActivity().onBackPressed()
            }
            .show()
    }

    private fun cargarHistorialClinico(username: String) {
        val docRef = firestore.collection("Pacientes").document(username)
        docRef.get().addOnSuccessListener { documento ->
            val datos = documento.data ?: return@addOnSuccessListener

            // Deshabilitar los campos
            editTextNombres.setText(datos["Nombres"] as? String)
            editTextApellidos.setText(datos["Apellidos"] as? String)
            editTextEdad.setText(datos["Edad"] as? String)
            editTextTalla.setText(datos["Talla"] as? String)
            editTextPeso.setText(datos["Peso"] as? String)
            editTextIMC.setText(datos["IMC"] as? String)
            editTextTemperatura.setText(datos["Temperatura"] as? String)
            editTextFrecuenciaRespiratoria.setText(datos["FrecuenciaRespiratoria"] as? String)
            editTextFrecuenciaCardiaca.setText(datos["FrecuenciaCardiaca"] as? String)
            editTextTensionArterial.setText(datos["TensionArterial"] as? String)
            editTextTabaquismo.setText(datos["Tabaquismo"] as? String)
            editTextAlcoholismo.setText(datos["Alcoholismo"] as? String)
            editTextSedentarismo.setText(datos["Sedentarismo"] as? String)
            editTextHabitosAlimenticios.setText(datos["HabitosAlimenticios"] as? String)
            editTextTipoDiabetes.setText(datos["TipoDiabetes"] as? String)
            editTextHipertensionArterial.setText(datos["HipertensionArterial"] as? String)
            editTextDislipidemia.setText(datos["Dislipidemia"] as? String)
            editTextObesidad.setText(datos["Obesidad"] as? String)
            editTextControlGlicemico.setText(datos["ControlGlicemico"] as? String)
            editTextManejoPieDiabetico.setText(datos["ManejoPieDiabetico"] as? String)
            editTextControlComorbilidades.setText(datos["ControlComorbilidades"] as? String)

            // Deshabilitar los campos de entrada
            editTextNombres.isEnabled = false
            editTextApellidos.isEnabled = false
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

            // Ocultar el botón de guardar
            buttonGuardar.visibility = View.GONE

            // Mostrar el cuadro de diálogo para tomar una foto
            mostrarDialogoFoto()
        }
    }


    private fun mostrarDialogoFoto() {
        AlertDialog.Builder(requireContext())
            .setTitle("Atención")
            .setMessage("Para analizar tus imágenes procede a la galería")
            .setPositiveButton("Ir a galería") { _, _ ->
                // Navegar al AnalizarFragment usando la función global
                (requireActivity() as MainView).openFragment(AnalizarFragment(), "AnalizarFragmentTag")
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Acción al cancelar, si es necesario
            }
            .show()
    }



}
