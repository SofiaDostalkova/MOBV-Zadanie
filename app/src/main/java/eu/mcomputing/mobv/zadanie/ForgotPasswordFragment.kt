package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]

        val emailInput = view.findViewById<TextInputEditText>(R.id.emailInput)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val backToLogin = view.findViewById<TextView>(R.id.backToLogin)

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.requestPasswordReset(email)
            } else {
                Snackbar.make(submitButton, "Email cannot be empty", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.resetResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(submitButton, "Check your email for reset link", Snackbar.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Snackbar.make(submitButton, "Failed to send reset email", Snackbar.LENGTH_SHORT).show()
            }
        }

        backToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}

