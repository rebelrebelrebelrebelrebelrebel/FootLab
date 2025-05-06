package com.example.tuapp.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.footlab.GaleriaFragment
import com.example.footlab.HistorialClinicoFragment
import com.example.tuapp.PerfilFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Número de pestañas

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PerfilFragment() // Fragmento para la pestaña "Perfil"
            1 -> HistorialClinicoFragment() // Fragmento para la pestaña "Historial Clínico"
            2 -> GaleriaFragment()
            else -> throw IllegalArgumentException("Posición inválida")
        }
    }
}
