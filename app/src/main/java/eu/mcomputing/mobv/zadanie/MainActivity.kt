package eu.mcomputing.mobv.zadanie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layoutcv2)
       /* ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        Log.d("TAG", "Moja sprava");

     /*   val myButton1: Button = findViewById(R.id.button1)
        val myButton2: Button = findViewById(R.id.button2)

        val mytext: TextView = findViewById(R.id.textView)
        myButton1.setOnClickListener {
            mytext.text = "Button 1 kliknuty"
        }
        myButton2.setOnClickListener {
            mytext.text = "Button 2 kliknuty"
        }
*/
        val intent = Intent(this, InputActivity::class.java)
        startActivity(intent)
    }
}