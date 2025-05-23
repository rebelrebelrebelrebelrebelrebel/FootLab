package com.example.footlab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginView : AppCompatActivity() {

    private lateinit var User: EditText
    private lateinit var Pass: EditText
    private lateinit var botonLogin: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_view)  // Inflar la UI primero

        // Inicializar vistas y Firebase
        User = findViewById(R.id.cajaCorreo)
        Pass = findViewById(R.id.cajaPassword)
        botonLogin = findViewById(R.id.botonLogin)
        db = FirebaseFirestore.getInstance()

        // Verificar si ya hay sesión activa
        verificarSesionActiva()

        botonLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun verificarSesionActiva() {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("Username", null)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (!savedUsername.isNullOrEmpty() && !pacienteId.isNullOrEmpty()) {
            Log.d("LoginView", "Sesión activa detectada, redirigiendo a MainView")
            val intent = Intent(this, MainView::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun iniciarSesion() {
        val username = User.text.toString().trim()
        val password = Pass.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        val campoBusqueda = if (username.contains("@")) "Correo" else "Nombre"
        buscarUsuarioYCargarCredenciales(username, password, campoBusqueda)
    }

    private fun buscarUsuarioYCargarCredenciales(usuario: String, contra: String, campo: String) {
        // Obtener todos los pacientes y buscar el usuario en sus credenciales
        db.collection("Pacientes").get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var loginEncontrado = false
                var busquedasPendientes = documentos.size()

                for (pacienteDoc in documentos) {
                    val pacienteId = pacienteDoc.id

                    db.collection("Pacientes")
                        .document(pacienteId)
                        .collection("Credenciales")
                        .document("Login")
                        .get()
                        .addOnSuccessListener { credDoc ->
                            busquedasPendientes--

                            val correo = credDoc.getString("Correo")
                            val nombreUsuario = credDoc.getString("Nombre")
                            val password = credDoc.getString("Contraseña")

                            if (!loginEncontrado) {
                                if ((campo == "Correo" && correo == usuario) || (campo == "Nombre" && nombreUsuario == usuario)) {
                                    if (password == contra) {
                                        loginEncontrado = true
                                        // Guardar solo datos necesarios, NO guardar contraseña
                                        guardarDatos(nombreUsuario ?: "", usuario, pacienteId)

                                        Toast.makeText(this, "Bienvenido $nombreUsuario", Toast.LENGTH_LONG).show()

                                        val intent = Intent(this, MainView::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            if (busquedasPendientes == 0 && !loginEncontrado) {
                                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            busquedasPendientes--
                            Log.e("LoginView", "Error al acceder a Credenciales: ${it.message}")
                            if (busquedasPendientes == 0 && !loginEncontrado) {
                                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar usuarios: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginView", "Fallo al acceder a la colección Pacientes: ${it.message}")
            }
    }

    private fun guardarDatos(nombre: String, user: String, pacienteId: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Nombre", nombre)
        editor.putString("Username", user)
        editor.putString("PacienteID", pacienteId)
        editor.apply()
    }

    // Función para cerrar sesión (limpia SharedPreferences y regresa a login)
    fun cerrarSesion() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginView::class.java)
        startActivity(intent)
        finish()
    }
}
