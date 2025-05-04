package com.example.footlab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FechaAdapter(
    private val fechas: List<Pair<String, HashMap<String, Any>>>,
    private val onFechaClick: (String, HashMap<String, Any>) -> Unit
) : RecyclerView.Adapter<FechaAdapter.FechaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fecha, parent, false)
        return FechaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
        val (fecha, resultado) = fechas[position]
        holder.bind(fecha, resultado, onFechaClick)
    }

    override fun getItemCount(): Int = fechas.size

    class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)

        fun bind(
            fecha: String,
            resultado: HashMap<String, Any>,
            onFechaClick: (String, HashMap<String, Any>) -> Unit
        ) {
            textViewFecha.text = fecha
            itemView.setOnClickListener {
                // Añadir una verificación para asegurarse de que los datos están completos
                if (resultado.isNotEmpty()) {
                    onFechaClick(fecha, resultado)
                }
            }
        }
    }
}
