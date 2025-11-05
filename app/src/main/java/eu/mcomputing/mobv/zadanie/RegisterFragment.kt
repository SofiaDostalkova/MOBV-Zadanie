package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]

        val usernameInput = view.findViewById<EditText>(R.id.editTextUsername)
        val emailInput = view.findViewById<EditText>(R.id.editText1)
        val passwordInput = view.findViewById<EditText>(R.id.editText2)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val signinButton = view.findViewById<Button>(R.id.signinButton)

        viewModel.registrationResult.observe(viewLifecycleOwner) {
            if (it.second != null) {
                Snackbar.make(submitButton, it.first, Snackbar.LENGTH_LONG).show()
                usernameInput.text.clear()
                emailInput.text.clear()
                passwordInput.text.clear()
            } else {
                Snackbar.make(submitButton, it.first, Snackbar.LENGTH_SHORT).show()
            }
        }

        submitButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            viewModel.registerUser(username, email, password)
        }

        signinButton.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}
