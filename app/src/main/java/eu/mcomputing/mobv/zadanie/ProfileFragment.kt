package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel

    // --- Step 1: Permissions setup ---
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted → enable location sharing
            Snackbar.make(requireView(), "Location permission granted!", Snackbar.LENGTH_SHORT).show()
            profileViewModel.sharingLocation.postValue(true)
        } else {
            // Permission denied → disable location sharing
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

        // --- Initialize your ViewModels ---
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

        // --- Load saved location sharing state ---
        profileViewModel.sharingLocation.postValue(
            PreferenceData.getInstance().getSharing(requireContext())
        )

        // --- Observe the location sharing switch ---
        profileViewModel.sharingLocation.observe(viewLifecycleOwner) { enabled ->
            enabled?.let {
                if (it) {
                    // User wants to enable sharing
                    if (!hasPermissions()) {
                        // Permission not granted → reset switch and request permission
                        profileViewModel.sharingLocation.postValue(false)
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        // Permission granted → save preference
                        PreferenceData.getInstance().putSharing(requireContext(), true)
                        locationSwitch.isChecked = true
                    }
                } else {
                    // User disables sharing → save preference
                    PreferenceData.getInstance().putSharing(requireContext(), false)
                    locationSwitch.isChecked = false
                }
            }
        }

        // --- Handle user toggling the switch ---
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.sharingLocation.postValue(isChecked)
        }

        // --- Request permission at startup if user wants sharing enabled ---
        if (!hasPermissions() && profileViewModel.sharingLocation.value == true) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
