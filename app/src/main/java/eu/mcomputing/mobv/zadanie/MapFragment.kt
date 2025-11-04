package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import eu.mcomputing.mobv.zadanie.R

class MapFragment : Fragment(R.layout.fragment_map) {

    private var mapView: MapView? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var mapView: MapView? = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        val navController = findNavController()
        bottomBar.setupWithNavController(navController)
    }

}