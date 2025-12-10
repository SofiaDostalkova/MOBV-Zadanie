package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.os.Build
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.net.Uri
import android.os.AsyncTask
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

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

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            handleSelectedPhoto(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun loadImageFromUrl(url: String, imageView: ImageView) {
        Glide.with(this)
            .load(url)
            .circleCrop()
            .placeholder(R.drawable.outline_account_circle_24)
            .error(R.drawable.outline_account_circle_24)
            .into(imageView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val choosePhotoButton = view.findViewById<Button>(R.id.chooseImageButton)
        val deletePhotoButton = view.findViewById<Button>(R.id.deleteImageButton)

        val user = PreferenceData.getInstance().getUser(requireContext())
        user?.let {
            usernameText.text = it.username
            emailText.text = it.email
            user?.photo?.takeIf { it.isNotEmpty() }?.let { photo ->
                val fullUrl = "https://upload.mcomputing.eu/$photo"
                loadImageFromUrl(fullUrl, profileImage)
            } ?: profileImage.setImageResource(R.drawable.outline_account_circle_24)
        }

        choosePhotoButton.setOnClickListener {
            if (checkPermissionsForGallery(true)) {
                openGallery()
            }
        }

        deletePhotoButton.setOnClickListener {
            lifecycleScope.launch {
                val currentUser = PreferenceData.getInstance().getUser(requireContext()) ?: return@launch
                val (success, _) = profileViewModel.getDataRepository().deleteUserPhoto(currentUser.access)
                if (success) {
                    profileImage.setImageResource(R.drawable.outline_account_circle_24)
                    Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        logoutButton.setOnClickListener {
            PreferenceData.getInstance().clearData(requireContext())
            authViewModel.logout()
            lifecycleScope.launch {
                profileViewModel.getDataRepository().clearCachedUsers()
            }

            Snackbar.make(requireView(), "Logged out successfully", Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        val changePasswordButton = view.findViewById<Button>(R.id.changePasswordButton)
        changePasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_changePasswordFragment)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                if (profileViewModel.sharingLocation.value == true) {
                    sendLocationToServer(location.latitude, location.longitude)
                }
            }
        }

        profileViewModel.sharingLocation.postValue(
            PreferenceData.getInstance().getSharing(requireContext())
        )

        profileViewModel.sharingLocation.observe(viewLifecycleOwner) { enabled ->
            enabled?.let {
                if (it) {
                    if (!hasPermissions()) {
                        profileViewModel.sharingLocation.postValue(false)
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        PreferenceData.getInstance().putSharing(requireContext(), true)
                        locationSwitch.isChecked = true
                        startLocationUpdates()
                    }
                } else {
                    PreferenceData.getInstance().putSharing(requireContext(), false)
                    locationSwitch.isChecked = false
                    stopLocationUpdates()
                    val u = PreferenceData.getInstance().getUser(requireContext()) ?: return@let
                    viewLifecycleOwner.lifecycleScope.launch {
                        profileViewModel.getDataRepository().apiDeleteGeofence(u.access)
                        profileViewModel.getDataRepository().clearCachedUsers()
                    }
                }
            }
        }

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.sharingLocation.postValue(isChecked)
        }

        if (!hasPermissions() && profileViewModel.sharingLocation.value == true) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun openGallery() {
        lifecycleScope.launch {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("image/jpeg"))
            )
        }
    }

    private fun handleSelectedPhoto(uri: Uri) {
        inputStreamToFile(uri)?.let { file ->
            val profileImage = view?.findViewById<ImageView>(R.id.profileImage) ?: return
            profileImage.setImageURI(Uri.fromFile(file))

            val user = PreferenceData.getInstance().getUser(requireContext()) ?: return
            lifecycleScope.launch {
                val (success, _) = profileViewModel.getDataRepository().uploadUserPhoto(user.access, file)
                if (success) {
                    PreferenceData.getInstance().updatePhoto(requireContext(), file.name)
                    Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun inputStreamToFile(uri: Uri): File? {
        val resolver = requireContext().applicationContext.contentResolver
        resolver.openInputStream(uri).use { inputStream ->
            var orig = File(requireContext().filesDir, "photo_copied.jpg")
            if (orig.exists()) orig.delete()
            orig = File(requireContext().filesDir, "photo_copied.jpg")

            FileOutputStream(orig).use { fos ->
                if (inputStream == null) {
                    Log.d("vybrane", "stream null")
                    return null
                }
                try {
                    Log.d("vybrane", "copied")
                    inputStream.copyTo(fos)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }
            }
            Log.d("PhotoPicker", "Copied file path: ${orig.absolutePath}")
            return orig
        }
    }

    private fun checkPermissionsForGallery(ask: Boolean = false): Boolean {
        val check = allPermissionsGrantedForGallery()
        if (ask && !check) {
            requestGalleryPermission.launch(REQUIRED_PERMISSIONS()[0])
        }
        return check
    }

    private fun allPermissionsGrantedForGallery(): Boolean {
        return REQUIRED_PERMISSIONS().all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun REQUIRED_PERMISSIONS(): Array<String> {
        return if (Build.VERSION.SDK_INT < 33) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT == 33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        }
    }

    private fun hasPermissions(): Boolean {
        return PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startLocationUpdates() {
        if (!hasPermissions()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun sendLocationToServer(lat: Double, lon: Double) {
        val user = PreferenceData.getInstance().getUser(requireContext()) ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val success = profileViewModel.getDataRepository().apiUpdateGeofence(lat, lon, 100, user.access)
            if (!success) Snackbar.make(requireView(), "Failed to send location", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}