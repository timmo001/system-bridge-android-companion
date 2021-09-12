package dev.timmo.systembridge

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.buttonSetupBridge).setOnClickListener {
            val host = findViewById<TextInputEditText>(R.id.editTextHost).text
            val apiPort = findViewById<TextInputEditText>(R.id.editTextApiPort).text
            val wsPort = findViewById<TextInputEditText>(R.id.editTextWsPort).text
            val apiKey = findViewById<TextInputEditText>(R.id.editTextApiKey).text

//            finish()
        }
    }
}
