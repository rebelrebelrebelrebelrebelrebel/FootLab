package com.example.footlab

import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ResultadosClasificadosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_clasificados)

        val jsonString = intent.getStringExtra("RESULTADO_JSON")
        if (jsonString != null) {
            val json = JSONObject(jsonString)

            // Cargar imagen clasificada (base64)
            val clasificadaBase64 = json.optString("imagen_clasificada", "")
            val clasificadaBitmap = clasificadaBase64.decodeBase64ToBitmap()
            findViewById<ImageView>(R.id.classificationImageView).setImageBitmap(clasificadaBitmap)

            // Cargar imagen de segmentación (base64)
            val segmentacionBase64 = json.optString("imagen_segmentacion", "")
            val segmentacionBitmap = segmentacionBase64.decodeBase64ToBitmap()
            findViewById<ImageView>(R.id.tissuesegmentationImageView).setImageBitmap(segmentacionBitmap)

            // Porcentajes
            findViewById<TextView>(R.id.porcentajeRojoTextView).text =
                "Epitelial (rojo): ${json.optDouble("porcentaje_rojo", 0.0)}%"
            findViewById<TextView>(R.id.porcentajeAmarilloTextView).text =
                "Esfacelar (amarillo): ${json.optDouble("porcentaje_amarillo", 0.0)}%"
            findViewById<TextView>(R.id.porcentajeAzulTextView).text =
                "Necrosado (azul): ${json.optDouble("porcentaje_azul", 0.0)}%"

            findViewById<TextView>(R.id.areaTextView).text =
                "Área de la herida: ${json.optDouble("area", 0.0)} px²"
            findViewById<TextView>(R.id.perimetroTextView).text =
                "Perímetro de la herida: ${json.optDouble("perimetro", 0.0)} px"

            // Tejido segmentado
            findViewById<TextView>(R.id.porcentajeCallosoTextView).text =
                "Calloso: ${json.optDouble("porcentaje_calloso", 0.0)}%"
            findViewById<TextView>(R.id.porcentajeFibrinaTextView).text =
                "Fibrina: ${json.optDouble("porcentaje_fibrina", 0.0)}%"
            findViewById<TextView>(R.id.porcentajeGranulacionTextView).text =
                "Granulación: ${json.optDouble("porcentaje_granulacion", 0.0)}%"

            findViewById<TextView>(R.id.tamanoCallosoTextView).text =
                "Tamaño Calloso: ${json.optDouble("tamano_calloso", 0.0)} px²"
            findViewById<TextView>(R.id.tamanoFibrinaTextView).text =
                "Tamaño Fibrina: ${json.optDouble("tamano_fibrina", 0.0)} px²"
            findViewById<TextView>(R.id.tamanoGranuladoTextView).text =
                "Tamaño Granulado: ${json.optDouble("tamano_granulado", 0.0)} px²"
        }
    }

    private fun String.decodeBase64ToBitmap(): android.graphics.Bitmap? {
        return try {
            val decodedBytes = Base64.decode(this, Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
