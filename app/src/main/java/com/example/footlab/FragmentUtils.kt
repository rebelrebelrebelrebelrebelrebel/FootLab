import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.example.footlab.R

// Global function to open fragments
fun FragmentActivity.openFragment(fragment: Fragment, tag: String? = null, homeFragmentTag: String? = "HOME_FRAGMENT") {
    val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.fragment_container, fragment, tag)

    // Add to the back stack unless it's the home fragment
    if (tag != homeFragmentTag) {
        fragmentTransaction.addToBackStack(tag)
    }

    fragmentTransaction.commit()
}
