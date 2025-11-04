package eu.mcomputing.mobv.zadanie

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val submitButton: Button = view.findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val input1: String = view.findViewById<EditText>(R.id.editText1).text.toString()
            val input2: String = view.findViewById<EditText>(R.id.editText2).text.toString()
        }

        val signinButton: Button = view.findViewById(R.id.signinButton)
        signinButton.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)

        }
    }
}