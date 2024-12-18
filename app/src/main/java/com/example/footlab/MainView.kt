package com.example.footlab

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.footlab.databinding.ActivityMainViewBinding
import com.example.tuapp.PerfilFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Date

class MainView : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainViewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val REQUEST_CAMERA_PERMISSION = 200
        const val HOME_FRAGMENT_TAG = "home_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_gallery -> {
                    Log.d("MainView", "Gallery clicked")  // Add a log to track interaction
                    openGallery()
                    true
                }
                R.id.bottom_help -> {
                    Log.d("MainView", "Help clicked")  // Log to see if the event is triggered
                    openFragment(AyudaFragment())
                    true
                }
                else -> false
            }
        }


        openFragment(HomeFragment(), HOME_FRAGMENT_TAG)

        binding.fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
                )
            } else {
                openCamera()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            showAlert("No se puede abrir la cámara")
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK -> {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                imageBitmap?.let { uploadImageToFirebase(it) } ?: showAlert("Error al capturar la imagen")
            }
            requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK -> {
                val imageUri = data?.data
                imageUri?.let { handleImageFromGallery(it) } ?: showAlert("Error al seleccionar la imagen")
            }
        }
    }

    private fun handleImageFromGallery(imageUri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        uploadImageToFirebase(bitmap)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Mensaje")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create().show()
        }
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val storageRef = storage.reference.child("${System.currentTimeMillis()}.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageInfoToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al cargar la imagen")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    private fun saveImageInfoToFirestore(imageUrl: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"

            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val batch = firestore.batch()

                        documentos.documents.forEach { paciente ->
                            val docRef = paciente.reference
                            val newPhoto = mapOf("Fecha" to Timestamp(Date()), "URL" to imageUrl)
                            batch.update(docRef, "Fotos", FieldValue.arrayUnion(newPhoto))
                        }

                        batch.commit()
                            .addOnSuccessListener { showAlert("Imagen guardada exitosamente") }
                            .addOnFailureListener { showAlert("Error al guardar la imagen") }
                    } else {
                        showAlert("Usuario no encontrado")
                    }
                }
                .addOnFailureListener { showAlert("Error al recuperar los datos del usuario") }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
            R.id.nav_profile -> openFragment(PerfilFragment())
            R.id.nav_galeria -> openFragment(AnalizarFragment())
            R.id.nav_progreso -> openFragment(HistorialFragment())
            R.id.nav_logout -> cerrarSesion()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is HomeFragment) {
                super.onBackPressed()
            } else {
                openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
            }
        }
    }

    private fun cerrarSesion() {
        getSharedPreferences("UserData", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginView::class.java))
        finish()
    }

    fun openFragment(fragment: Fragment, tag: String? = null) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag)

        // Only add to the back stack if it's not the HomeFragment
        if (tag != HOME_FRAGMENT_TAG) {
            fragmentTransaction.addToBackStack(tag)
        }

        fragmentTransaction.commit()

        // Visibility handling for BottomAppBar, BottomNavigationView, and FAB
        when (fragment) {
            is HomeFragment -> {
                binding.bottomAppBar.visibility = View.GONE
                binding.bottomNavigation.visibility = View.GONE
                binding.fab.visibility = View.GONE
            }
            else -> {
                binding.bottomAppBar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
}
        }
    }

}

