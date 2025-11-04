package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import eu.mcomputing.mobv.zadanie.R

class MapFragment : Fragment(R.layout.fragment_map) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        val navController = findNavController()
        bottomBar.setupWithNavController(navController)
    }
}