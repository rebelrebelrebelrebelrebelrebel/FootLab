package com.example.footlab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FechaAdapter(
    private val fechas: List<String>,
    private val onFechaClick: (String) -> Unit
) : RecyclerView.Adapter<FechaAdapter.FechaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fecha, parent, false)
        return FechaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
        val fecha = fechas[position]
        holder.bind(fecha, onFechaClick)
    }

    override fun getItemCount(): Int = fechas.size

    class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(fecha: String, onFechaClick: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.textViewFecha).text = fecha
            itemView.setOnClickListener {
                onFechaClick(fecha) // Llama a la función para manejar el clic
            }
        }
    }
}