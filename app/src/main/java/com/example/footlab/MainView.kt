package com.example.footlab

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.footlab.databinding.ActivityMainViewBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class MainView : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainViewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var sharedPreferences: SharedPreferences


    companion object {
        private const val TAG = "MainView"
        private const val HOME_FRAGMENT_TAG = "home_fragment"
    }

    // ActivityResultLaunchers para cámara y galería
    private lateinit var takePictureLauncher: ActivityResultLauncher<Void?>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

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
                    Log.d(TAG, "Gallery clicked")
                    openGallery()
                    true
                }
                R.id.bottom_help -> {
                    Log.d(TAG, "Help clicked")
                    openFragment(AyudaFragment())
                    true
                }
                else -> false
            }
        }

        openFragment(HomeFragment(), HOME_FRAGMENT_TAG)

        // Registrar los ActivityResultLaunchers
        setupActivityResultLaunchers()

        binding.fab.setOnClickListener {
            checkPermissionsAndOpenCamera()
        }
    }

    private fun setupActivityResultLaunchers() {
        // Cámara: no recibe datos, solo resultado OK/Cancel
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                Log.d(TAG, "Imagen capturada con cámara")
                uploadImageToFirebase(bitmap)
            } else {
                showAlert("Error al capturar la imagen")
            }
        }

        // Galería: devuelve URI de imagen seleccionada
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Imagen seleccionada de galería: $uri")
                handleImageFromGallery(uri)
            } else {
                showAlert("Error al seleccionar la imagen")
            }
        }

        // Permisos para cámara y almacenamiento (lectura)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                var allGranted = true
                permissions.entries.forEach {
                    Log.d(TAG, "Permiso ${it.key} = ${it.value}")
                    if (!it.value) allGranted = false
                }
                if (allGranted) {
                    Log.d(TAG, "Todos los permisos concedidos")
                    openCamera()
                } else {
                    Toast.makeText(this, "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun checkPermissionsAndOpenCamera() {
        val neededPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA)
        }
        // Para Android < 13, pide READ_EXTERNAL_STORAGE para guardar o leer imágenes
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        // Para Android 13+ se pedirían permisos diferentes para medios (si necesario)
        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        takePictureLauncher.launch(null)
    }

    private fun openGallery() {
        // Aquí el tipo "image/*" para filtrar sólo imágenes
        pickImageLauncher.launch("image/*")
    }

    private fun handleImageFromGallery(imageUri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
            uploadImageToFirebase(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error al decodificar imagen desde galería", e)
            showAlert("Error al procesar la imagen de la galería")
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
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (pacienteId != null) {
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
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("PacienteID", null)

        if (pacienteId != null) {
            val pacienteRef = firestore.collection("Pacientes").document(pacienteId)
            val imagenesRef = pacienteRef.collection("Imagenes")

            // Para asignar nombre único "foto1", "foto2", ... consultamos cuántos documentos hay
            imagenesRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val fotoCount = querySnapshot.size() // número actual de fotos
                    val nombreFoto = "Foto${fotoCount + 1}"

                    val nuevaFoto = hashMapOf(
                        "URL" to imageUrl,
                        "Fecha" to Timestamp.now()
                    )

                    imagenesRef.document(nombreFoto).set(nuevaFoto)
                        .addOnSuccessListener {
                            showAlert("Foto guardada exitosamente")
                        }
                        .addOnFailureListener {
                            showAlert("Error al guardar la foto")
                        }
                }
                .addOnFailureListener {
                    showAlert("Error al obtener las fotos existentes")
                }

        } else {
            showAlert("Usuario no encontrado")
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
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

        // Solo agrega a back stack si no es el HomeFragment
        if (tag != HOME_FRAGMENT_TAG) {
            fragmentTransaction.addToBackStack(tag)
        }

        fragmentTransaction.commit()

        // Control de visibilidad basado en el fragmento
        when (fragment) {
            is HomeFragment, is ResultadosFragment -> {
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
