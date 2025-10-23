package eu.mcomputing.mobv.zadanie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val input1: String = findViewById<EditText>(R.id.editText1).text.toString()
            val input2: String = findViewById<EditText>(R.id.editText2).text.toString()

        }

        val signinButton: Button = findViewById(R.id.signinButton)
        signinButton.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)

        }
    }
}