package com.example.footlab

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificarCredencialesGuardadas()
        setContentView(R.layout.activity_login_view)

        // Inicialización de elementos UI
        User = findViewById(R.id.cajaCorreo)
        Pass = findViewById(R.id.cajaPassword)
        botonLogin = findViewById(R.id.botonLogin)

        // Inicialización de Firestore
        db = FirebaseFirestore.getInstance()

        // Evento del botón
        botonLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun verificarCredencialesGuardadas() {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("Username", null)
        val savedPassword = sharedPreferences.getString("Password", null)
        if (savedUsername != null && savedPassword != null) {
            val intent = Intent(this, MainView::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun iniciarSesion() {
        username = User.text.toString().trim()
        password = Pass.text.toString().trim()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            if (username.contains("@")) {
                login(username, password, "Email")
            } else {
                login(username, password, "UserName")
            }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_LONG).show()
        }
    }

    private fun login(usuario: String, contra: String, campo: String) {
        db.collection("pacientes").get().addOnSuccessListener { documentos ->
            if (documentos.isEmpty) {
                Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show()
            } else {
                var loginExitoso = false
                for (pacienteDoc in documentos.documents) {
                    val pacienteId = pacienteDoc.id

                    db.collection("pacientes")
                        .document(pacienteId)
                        .collection("Credenciales")
                        .document("Login")
                        .get()
                        .addOnSuccessListener { credDoc ->
                            val correo = credDoc.getString("Correo")
                            val nombreUsuario = credDoc.getString("Nombre")
                            val password = credDoc.getString("Contraseña")

                            // Verificar si el usuario y la contraseña coinciden
                            if ((campo == "Email" && correo == usuario) || (campo == "UserName" && nombreUsuario == usuario)) {
                                if (password == contra) {
                                    loginExitoso = true
                                    Log.d("LoginView", "PacienteID autenticado: $pacienteId") // <-- Log agregado aquí
                                    guardarDatos(nombreUsuario ?: "", usuario, contra, pacienteId)
                                    Toast.makeText(this, "Bienvenido $nombreUsuario", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, MainView::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                }

                // Solo muestra el mensaje de error si no se encuentra un usuario válido
                Handler(mainLooper).postDelayed({
                    if (!loginExitoso) {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }, 1500)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al buscar usuarios: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun guardarDatos(nombre: String, user: String, pass: String, pacienteId: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Nombre", nombre)
        editor.putString("Username", user)
        editor.putString("Password", pass)
        editor.putString("PacienteID", pacienteId)
        editor.apply()
    }
}
