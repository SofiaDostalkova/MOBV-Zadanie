package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.android.gestures.MoveGestureDetector

class MapFragment : Fragment(R.layout.fragment_map) {

    private var mapView: MapView? = null
    private var lastLocation: Point? = null
    private var selectedPoint: CircleAnnotation? = null
    private var annotationManager: CircleAnnotationManager? = null

    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initLocationComponent()
                addLocationListeners()
            }
        }

    private fun hasPermissions(): Boolean {
        return PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        // setup bottom bar if you have it
        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar?.setupWithNavController(findNavController())

        // annotation manager
        mapView?.let {
            annotationManager = it.annotations.createCircleAnnotationManager()
        }

        val hasPermission = hasPermissions()
        onMapReady(hasPermission)

        val myLocationBtn = view.findViewById<ImageView>(R.id.my_location)
        myLocationBtn.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                lastLocation?.let { refreshLocation(it) }
                addLocationListeners()
                Log.d("MapFragment", "location click")
            }
        }
    }

    private fun onMapReady(enabled: Boolean) {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(14.3539484, 49.8001304))
                .zoom(2.0)
                .build()
        )
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS) {
            if (enabled) {
                initLocationComponent()
                addLocationListeners()
            }
        }

        mapView?.getMapboxMap()?.addOnMapClickListener {
            if (hasPermissions()) {
                onCameraTrackingDismissed()
            }
            true
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        Log.d("MapFragment", "poloha je $it")
        refreshLocation(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean = false
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun initLocationComponent() {
        mapView?.location?.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }
    }

    private fun addLocationListeners() {
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun refreshLocation(point: Point) {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(point)
        lastLocation = point
        addMarker(point)
    }

    private fun addMarker(point: Point) {
        annotationManager?.let { manager ->
            if (selectedPoint == null) {
                manager.deleteAll()
                val options = CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(100.0)
                    .withCircleOpacity(0.2)
                    .withCircleColor("#000")
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#ffffff")
                selectedPoint = manager.create(options)
            } else {
                selectedPoint?.let {
                    it.point = point
                    manager.update(it)
                }
            }
        }
    }

    private fun onCameraTrackingDismissed() {
        mapView?.location?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onCameraTrackingDismissed()
        mapView = null
    }
}
