package eu.mcomputing.mobv.zadanie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class InputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val input1: String = findViewById<EditText>(R.id.editText1).text.toString()
            val input2: String = findViewById<EditText>(R.id.editText2).text.toString()

        }

        val signinButton: Button = findViewById(R.id.signinButton)
        signinButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
    }
}