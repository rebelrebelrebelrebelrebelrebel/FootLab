package com.example.footlab

//import HistorialClinicoFragment
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        // Initialize ViewPager with adapter
        viewPager.adapter = ViewPagerAdapter(this)

        // Set up TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Perfil"
                1 -> "Historial clínico"
                else -> null
            }
        }.attach()

        viewPager.setCurrentItem(0, false)

    }

    override fun onResume() {
        super.onResume()
        viewPager.setCurrentItem(0, false)
    }


    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2 // 3 tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PerfilFragment() // Make sure PerfilFragment is implemented
                1 -> HistorialClinicoFragment() // Make sure HistorialFragment is implemented
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }
    }
}
