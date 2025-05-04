package com.example.footlab

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File

class ResultadosClasificadosActivity : AppCompatActivity() {

    private var resultadoFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_clasificados)

        resultadoFileName = intent.getStringExtra("RESULTADO_FILE")

        if (resultadoFileName == null) {
            mostrarError("No se proporcionó el nombre del archivo.")
            finish()
            return
        }

        val file = File(cacheDir, resultadoFileName!!)
        if (!file.exists()) {
            mostrarError("No se encontró el archivo con los resultados.")
            finish()
            return
        }

        val resultado = try {
            val jsonString = file.readText()
            Log.d("ResultadosClasificados", "JSON leído: $jsonString")
            JSONObject(jsonString)
        } catch (e: Exception) {
            Log.e("ResultadosClasificados", "Error al leer los datos: ${e.message}")
            mostrarError("Error al leer los datos: ${e.message}")
            finish()
            return
        }


        mostrarResultados(resultado)
    }

    private fun mostrarResultados(resultado: JSONObject) {
        val porcentajeEpitelial = resultado.optDouble("PorcentajeEpitelial", 0.0)
        val porcentajeEsfacelar = resultado.optDouble("PorcentajeEsfacelar", 0.0)
        val porcentajeNecrosado = resultado.optDouble("PorcentajeNecrosado", 0.0)

        val area = resultado.optDouble("Area", 0.0)
        val perimetro = resultado.optDouble("Perimetro", 0.0)

        val porcentajeCalloso = resultado.optDouble("PorcentajeCalloso", 0.0)
        val porcentajeFibrina = resultado.optDouble("PorcentajeFibrina", 0.0)
        val porcentajeGranulacion = resultado.optDouble("PorcentajeGranulacion", 0.0)

        val tamanoCalloso = resultado.optDouble("TamanoCalloso", 0.0)
        val tamanoFibrina = resultado.optDouble("TamanoFibrina", 0.0)
        val tamanoGranulado = resultado.optDouble("TamanoGranulado", 0.0)

        // Establecer los valores en los TextViews
        findViewById<TextView>(R.id.porcentajeRojoTextView).text = "Epitelial (Rojo): ${porcentajeEpitelial}%"
        findViewById<TextView>(R.id.porcentajeAmarilloTextView).text = "Esfacelar (Amarillo): ${porcentajeEsfacelar}%"
        findViewById<TextView>(R.id.porcentajeAzulTextView).text = "Necrosado (Azul): ${porcentajeNecrosado}%"
        findViewById<TextView>(R.id.areaTextView).text = "Área de la herida: ${area} mm²"
        findViewById<TextView>(R.id.perimetroTextView).text = "Perímetro de la herida: ${perimetro} mm"
        findViewById<TextView>(R.id.porcentajeCallosoTextView).text = "Calloso: ${porcentajeCalloso}%"
        findViewById<TextView>(R.id.porcentajeFibrinaTextView).text = "Fibrina: ${porcentajeFibrina}%"
        findViewById<TextView>(R.id.porcentajeGranulacionTextView).text = "Granulación: ${porcentajeGranulacion}%"
        findViewById<TextView>(R.id.tamanoCallosoTextView).text = "Tamaño de Calloso: ${tamanoCalloso} mm²"
        findViewById<TextView>(R.id.tamanoFibrinaTextView).text = "Tamaño de Fibrina: ${tamanoFibrina} mm²"
        findViewById<TextView>(R.id.tamanoGranuladoTextView).text = "Tamaño de Granulado: ${tamanoGranulado} mm²"

        // Cargar las imágenes
        val imagenClasificadaBase64 = resultado.optString("ImagenClasificadaBase64", "")
        val imagenSegmentacionBase64 = resultado.optString("TissueSegmentationBase64", "")

        // Guardar las imágenes como archivos temporales
        val imagenClasificadaFile = guardarImagenEnArchivo(imagenClasificadaBase64, "imagen_clasificada.jpg")
        val imagenSegmentacionFile = guardarImagenEnArchivo(imagenSegmentacionBase64, "imagen_segmentacion.jpg")

        // Cargar las imágenes desde los archivos
        imagenClasificadaFile?.let { cargarImagenDesdeArchivo(it, R.id.classificationImageView) }
        imagenSegmentacionFile?.let { cargarImagenDesdeArchivo(it, R.id.tissuesegmentationImageView) }
    }

    private fun guardarImagenEnArchivo(base64String: String, fileName: String): File? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val file = File(cacheDir, fileName)
            val outputStream = file.outputStream()
            outputStream.write(decodedBytes)
            outputStream.close()
            Log.d("ResultadosClasificados", "Imagen guardada en: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("ResultadosClasificados", "Error al guardar la imagen: ${e.message}")
            mostrarError("Error al guardar la imagen: ${e.message}")
            null
        }
    }

    private fun cargarImagenDesdeArchivo(file: File, imageViewId: Int) {
        if (file.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Log.d("ResultadosClasificados", "Imagen cargada desde: ${file.absolutePath}")
                findViewById<ImageView>(imageViewId).setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("ResultadosClasificados", "Error al cargar la imagen desde el archivo: ${e.message}")
                mostrarError("Error al cargar la imagen desde el archivo: ${e.message}")
            }
        } else {
            Log.e("ResultadosClasificados", "El archivo de imagen no existe.")
            mostrarError("El archivo de imagen no existe.")
        }
    }


    private fun mostrarError(mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar archivos temporales al destruir la actividad
        val imagenClasificadaFile = File(cacheDir, "imagen_clasificada.jpg")
        val imagenSegmentacionFile = File(cacheDir, "imagen_segmentacion.jpg")

        imagenClasificadaFile.delete()
        imagenSegmentacionFile.delete()
    }
}
