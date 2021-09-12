package dev.timmo.systembridge

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.buttonSetupBridge).setOnClickListener {
            val host = findViewById<TextInputEditText>(R.id.editTextHost).text.toString()
            val apiPort =
                findViewById<TextInputEditText>(R.id.editTextApiPort).text.toString().toInt()
            val apiKey = findViewById<TextInputEditText>(R.id.editTextApiKey).text.toString()

            AppDatabase.getInstance(applicationContext).connectionDao().insert(
                Connection(
                    0,
                    host,
                    apiPort,
                    apiKey
                )
            )

            finish()
        }
    }
}
