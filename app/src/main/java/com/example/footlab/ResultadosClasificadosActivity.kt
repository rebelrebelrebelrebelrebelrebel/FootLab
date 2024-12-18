package com.example.footlab

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.footlab.MainView.Companion.HOME_FRAGMENT_TAG
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultadosClasificadosActivity : AppCompatActivity() {

    private lateinit var classificationImageView: ImageView
    private lateinit var classificationResultsTextView: TextView
    private lateinit var porcentajeRojoTextView: TextView
    private lateinit var porcentajeAmarilloTextView: TextView
    private lateinit var porcentajeAzulTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var firestore: FirebaseFirestore

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_clasificados)

        // Initialize views
        classificationImageView = findViewById(R.id.classificationImageView)
        porcentajeRojoTextView = findViewById(R.id.porcentajeRojoTextView)
        porcentajeAmarilloTextView = findViewById(R.id.porcentajeAmarilloTextView)
        porcentajeAzulTextView = findViewById(R.id.porcentajeAzulTextView)
        titleTextView = findViewById(R.id.titleTextView)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up the toolbar and navigation drawer
        setupToolbarAndDrawer()

        // Load data based on the selected date
        val selectedDate = getSelectedDate()
        if (selectedDate != null) {
            loadDataFromFirestore(selectedDate)
        } else {
            showToast("No se encontró la fecha seleccionada.")
        }
    }

    private fun setupToolbarAndDrawer() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationDrawer)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
                //R.id.nav_profile -> openFragment(PerfilFragment())
                R.id.nav_galeria -> openFragment(AnalizarFragment())
                R.id.nav_progreso -> openFragment(HistorialFragment())
                R.id.nav_logout -> cerrarSesion()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun openFragment(fragment: Fragment, tag: String? = null) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, tag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun cerrarSesion() {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginView::class.java)
        startActivity(intent)
        finish()
    }

    private fun getSelectedDate(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("FECHA_SELECCIONADA", null)
    }

    private fun loadDataFromFirestore(selectedDate: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username.isNullOrBlank()) {
            showToast("Usuario no encontrado.")
            return
        }

        val field = if (username.contains("@")) "Email" else "UserName"

        firestore.collection("Pacientes").whereEqualTo(field, username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (patient in documents.documents) {
                        val results = patient.get("Resultados") as? ArrayList<HashMap<String, Any>>

                        if (results.isNullOrEmpty()) {
                            showToast("No se encontraron resultados para el usuario.")
                            return@addOnSuccessListener
                        }

                        val formattedSelectedDate = parseDate(selectedDate)?.toCustomFormattedString()

                        val filteredResult = results.firstOrNull { result ->
                            val firestoreDate = (result["Fecha"] as? Timestamp)?.toDate()
                            firestoreDate?.toCustomFormattedString() == formattedSelectedDate
                        }

                        if (filteredResult != null) {
                            displayResults(filteredResult, selectedDate)
                        } else {
                            showToast("No se encontraron resultados para la fecha seleccionada.")
                        }
                    }
                } else {
                    showToast("Usuario no encontrado.")
                }
            }
            .addOnFailureListener {
                showToast("Error al recuperar los datos.")
            }
    }

    private fun parseDate(date: String): Date? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            inputFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    private fun displayResults(result: HashMap<String, Any>, selectedDate: String) {
        val imageUrl = result["URL"] as? String
        val porcentajeRojo = result["PorcentajeEpitelial"] as? Double ?: 0.0
        val porcentajeAmarillo = result["PorcentajeEsfacelar"] as? Double ?: 0.0
        val porcentajeAzul = result["PorcentajeNecrosado"] as? Double ?: 0.0

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(classificationImageView)
        }

        porcentajeRojoTextView.text = getString(R.string.porcentaje_epitelial_rojo2, porcentajeRojo)
        porcentajeAmarilloTextView.text = getString(R.string.porcentaje_esfacelar_amarillo2, porcentajeAmarillo)
        porcentajeAzulTextView.text = getString(R.string.porcentaje_necrosado_azul2, porcentajeAzul)

        titleTextView.text = selectedDate
    }

    private fun Date.toCustomFormattedString(): String {
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return outputFormat.format(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
