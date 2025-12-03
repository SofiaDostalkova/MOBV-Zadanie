package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // --- Permissions setup ---
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Snackbar.make(requireView(), "Location permission granted!", Snackbar.LENGTH_SHORT).show()
            profileViewModel.sharingLocation.postValue(true)
        } else {
            Snackbar.make(requireView(), "Location permission denied!", Snackbar.LENGTH_SHORT).show()
            profileViewModel.sharingLocation.postValue(false)
        }
    }

    private fun hasPermissions(): Boolean {
        return PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Initialize ViewModels ---
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        profileViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(DataRepository.getInstance(requireContext())) as T
                }
            }
        )[ProfileViewModel::class.java]

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar.setupWithNavController(findNavController())

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val usernameText = view.findViewById<TextView>(R.id.profileUsername)
        val emailText = view.findViewById<TextView>(R.id.profileEmail)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val locationSwitch = view.findViewById<SwitchCompat>(R.id.location_switch)

        val user = PreferenceData.getInstance().getUser(requireContext())
        user?.let {
            usernameText.text = it.username
            emailText.text = it.email
        }

        /* Uncomment if you want to show user info
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                usernameText.text = user.username
                emailText.text = user.email
            }
        }

        logoutButton.setOnClickListener {
            authViewModel.logout()
            Snackbar.make(view, getString(R.string.logout_snackbar), Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profile_to_login)
        }
        */

        // --- Initialize location client & callback ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                if (profileViewModel.sharingLocation.value == true) {
                    sendLocationToServer(location.latitude, location.longitude)
                }
            }
        }

        // --- Load saved location sharing state ---
        profileViewModel.sharingLocation.postValue(
            PreferenceData.getInstance().getSharing(requireContext())
        )

        // --- Observe the location sharing switch ---
        profileViewModel.sharingLocation.observe(viewLifecycleOwner) { enabled ->
            enabled?.let {
                if (it) {
                    // Enable sharing
                    if (!hasPermissions()) {
                        profileViewModel.sharingLocation.postValue(false)
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        PreferenceData.getInstance().putSharing(requireContext(), true)
                        locationSwitch.isChecked = true
                        startLocationUpdates()
                    }
                } else {
                    // Disable sharing
                    PreferenceData.getInstance().putSharing(requireContext(), false)
                    locationSwitch.isChecked = false
                    stopLocationUpdates()
                    val user = PreferenceData.getInstance().getUser(requireContext()) ?: return@let
                    viewLifecycleOwner.lifecycleScope.launch {
                        profileViewModel.getDataRepository().apiDeleteGeofence(user.access)
                        profileViewModel.getDataRepository().clearCachedUsers()
                    }
                }
            }
        }

        // --- Handle user toggling the switch ---
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.sharingLocation.postValue(isChecked)
        }

        // --- Request permission at startup if needed ---
        if (!hasPermissions() && profileViewModel.sharingLocation.value == true) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- Start/stop location updates ---
    private fun startLocationUpdates() {
        if (!hasPermissions()) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // --- Send location to server ---
    private fun sendLocationToServer(lat: Double, lon: Double) {
        val user = PreferenceData.getInstance().getUser(requireContext()) ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val success = profileViewModel.getDataRepository().apiUpdateGeofence(lat, lon, 500, user.access)
            if (!success) {
                Snackbar.make(requireView(), "Failed to send location", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}
