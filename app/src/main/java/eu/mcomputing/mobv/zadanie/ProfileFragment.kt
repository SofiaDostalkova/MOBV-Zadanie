package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar.setupWithNavController(findNavController())

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val usernameText = view.findViewById<TextView>(R.id.profileUsername)
        val emailText = view.findViewById<TextView>(R.id.profileEmail)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                usernameText.text = user.username
                emailText.text = user.email
            }
        }

        logoutButton.setOnClickListener {
            viewModel.logout()
            Snackbar.make(view, getString(R.string.logout_snackbar), Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profile_to_login)
        }

    }
}
