package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.gestures.gestures
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapFragment : Fragment(R.layout.fragment_map) {

    private var mapView: MapView? = null
    private var lastLocation: Point? = null
    private var geofenceCircle: CircleAnnotation? = null
    private var circleAnnotationManager: CircleAnnotationManager? = null
    private var pointAnnotationManager: PointAnnotationManager? = null

    private lateinit var feedViewModel: FeedViewModel

    private val foregroundPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    // Store stable user positions and annotations
    private val userPositions = mutableMapOf<String, Point>()
    private val userAnnotations = mutableMapOf<String, com.mapbox.maps.plugin.annotation.generated.PointAnnotation>()

    private val foregroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.all { it }) {
                startLocationSetup()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermissionLauncher.launch(backgroundPermission)
                }
            }
        }

    private val backgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun hasForegroundPermissions() =
        foregroundPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        circleAnnotationManager = mapView?.annotations?.createCircleAnnotationManager()
        pointAnnotationManager = mapView?.annotations?.createPointAnnotationManager()

        // Initialize FeedViewModel
        val repository = DataRepository.getInstance(requireContext())
        feedViewModel = FeedViewModel(repository, requireContext())

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar.setupWithNavController(findNavController())

        // Observe users from Room
        feedViewModel.users.observe(viewLifecycleOwner) { users ->
            lastLocation?.let { center ->
                showUsersOnMap(users.filterNotNull(), center)
            }
        }

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
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        refreshLocation(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) = removeCameraTrackingListeners()
        override fun onMove(detector: MoveGestureDetector) = false
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun initLocationComponent() {
        mapView?.location?.updateSettings { enabled = true; pulsingEnabled = true }
    }

    private fun addLocationListeners() {
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun getLastLocationAndSetupGeofence() {
        if (!hasForegroundPermissions()) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val point = if (location != null) {
                setupGeofence(location)
                Point.fromLngLat(location.longitude, location.latitude)
            } else {
                Point.fromLngLat(14.3539484, 49.8001304)
            }
            refreshLocation(point)
        }
    }

    private fun setupGeofence(location: android.location.Location) {
        GeofenceHelper.addGeofence(requireContext(), location)
    }

    private fun refreshLocation(point: Point) {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(point)
        lastLocation = point
        addGeofenceCircle(point)

        // Update users on map, positions stay stable
        feedViewModel.users.value?.let { users ->
            showUsersOnMap(users.filterNotNull(), point)
        }
    }

    private fun addGeofenceCircle(center: Point) {
        circleAnnotationManager?.let { manager ->
            if (geofenceCircle == null) {
                val options = CircleAnnotationOptions()
                    .withPoint(center)
                    .withCircleRadius(100.0)
                    .withCircleOpacity(0.2)
                    .withCircleColor("#000000")
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#ffffff")
                geofenceCircle = manager.create(options)
            } else {
                geofenceCircle?.let { it.point = center; manager.update(it) }
            }
        }
    }

    private fun showUsersOnMap(users: List<UserEntity>, center: Point) {
        users.forEach { user ->
            val uid = user.uid

            // Generate a random position once per user
            val position = userPositions.getOrPut(uid) {
                randomPointInCircle(center, 100.0)
            }

            // Skip if marker already exists
            if (userAnnotations.containsKey(uid)) return

            user.photo?.let { photoUrl ->
                Glide.with(this)
                    .asBitmap()
                    .load("https://upload.mcomputing.eu/$photoUrl")
                    .circleCrop()
                    .into(object : CustomTarget<Bitmap>(80, 80) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val options = PointAnnotationOptions()
                                .withPoint(position)
                                .withIconImage(resource)
                                .withIconSize(1.0)
                            val annotation = pointAnnotationManager?.create(options)
                            if (annotation != null) userAnnotations[uid] = annotation
                        }
                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                    })
            }
        }
    }

    private fun randomPointInCircle(center: Point, radiusMeters: Double): Point {
        val radiusInDegrees = (radiusMeters * 3) / 111000.0
        val u = Math.random()
        val v = Math.random()
        val w = radiusInDegrees * sqrt(u)*0.9
        val t = 2 * PI * v
        val x = w * cos(t)
        val y = w * sin(t)
        return Point.fromLngLat(center.longitude() + x, center.latitude() + y)
    }

    private fun removeCameraTrackingListeners() {
        mapView?.location?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeCameraTrackingListeners()
        mapView = null
    }
}
