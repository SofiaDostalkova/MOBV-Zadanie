package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.MapView
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures

class MapFragment : Fragment(R.layout.fragment_map) {

    private var mapView: MapView? = null
    private var lastLocation: Point? = null
    private var selectedPoint: CircleAnnotation? = null
    private var annotationManager: CircleAnnotationManager? = null

    private val foregroundPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private val foregroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.all { it }) {
                Log.d("MapFragment", "Foreground permissions granted")
                startLocationSetup()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermissionLauncher.launch(backgroundPermission)
                }
            } else {
                Log.w("MapFragment", "Foreground permissions not granted")
            }
        }

    private val backgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) Log.d("MapFragment", "Background permission granted")
            else Log.w("MapFragment", "Background permission not granted")
        }

    private fun hasForegroundPermissions() =
        foregroundPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar?.setupWithNavController(findNavController())

        mapView = view.findViewById(R.id.mapView)
        annotationManager = mapView?.annotations?.createCircleAnnotationManager()

        // Check permissions first, request if needed
        if (!hasForegroundPermissions()) {
            foregroundPermissionLauncher.launch(foregroundPermissions)
        } else {
            startLocationSetup()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundPermissionLauncher.launch(backgroundPermission)
            }
        }

        setupMapCamera()
    }

    private fun startLocationSetup() {
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS) {
            initLocationComponent()
            addLocationListeners()
            getLastLocationAndSetupGeofence()
        }
    }

    private fun setupMapCamera() {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(14.3539484, 49.8001304))
                .zoom(2.0)
                .build()
        )

        mapView?.getMapboxMap()?.addOnMapClickListener {
            if (hasForegroundPermissions()) {
                onCameraTrackingDismissed()
            }
            true
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        refreshLocation(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) = onCameraTrackingDismissed()
        override fun onMove(detector: MoveGestureDetector) = false
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun initLocationComponent() {
        Log.d("MapFragment", "initLocationComponent")
        mapView?.location?.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun addLocationListeners() {
        Log.d("MapFragment", "addLocationListeners")
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun getLastLocationAndSetupGeofence() {
        if (!hasForegroundPermissions()) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val point = if (location != null) {
                Log.d("MapFragment", "Last known location: ${location.latitude}, ${location.longitude}")
                setupGeofence(location)
                Point.fromLngLat(location.longitude, location.latitude)
            } else {
                Log.w("MapFragment", "Last location is null, using default")
                Point.fromLngLat(14.3539484, 49.8001304)
            }
            refreshLocation(point)
        }.addOnFailureListener {
            Log.e("MapFragment", "Failed to get location: ${it.message}")
            refreshLocation(Point.fromLngLat(14.3539484, 49.8001304))
        }
    }

    private fun setupGeofence(location: Location) {
        Log.d("MapFragment", "Setting up geofence at ${location.latitude}, ${location.longitude}")
        GeofenceHelper.addGeofence(requireContext(), location)
    }

    private fun refreshLocation(point: Point) {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder().center(point).zoom(14.0).build()
        )
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
                selectedPoint?.let { it.point = point; manager.update(it) }
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

