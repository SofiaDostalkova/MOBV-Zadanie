package eu.mcomputing.mobv.zadanie


import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private lateinit var dataRepository: DataRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataRepository = DataRepository.getInstance(requireContext())

        val oldPasswordInput = view.findViewById<TextInputEditText>(R.id.oldPasswordInput)
        val newPasswordInput = view.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val oldPass = oldPasswordInput.text.toString()
            val newPass = newPasswordInput.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Snackbar.make(saveButton, "Both fields are required", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = PreferenceData.getInstance().getUser(requireContext())
            if (user == null) {
                Snackbar.make(saveButton, "User not logged in", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = dataRepository.apiChangePassword(user.access, oldPass, newPass)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Snackbar.make(saveButton, "Password changed successfully", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    } else {
                        Snackbar.make(saveButton, "Failed to change password", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
