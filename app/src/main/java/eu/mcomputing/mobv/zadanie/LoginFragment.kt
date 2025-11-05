package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]

        val emailInput = view.findViewById<EditText>(R.id.editText1)
        val passwordInput = view.findViewById<EditText>(R.id.editText2)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val registerButton = view.findViewById<Button>(R.id.signinButton)

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            val (message, user) = result
            if (user != null) {
                Snackbar.make(submitButton, message, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_login_to_feed)
            } else {
                Snackbar.make(submitButton, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            viewModel.loginUser(email, password)
        }

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
}
