package dev.timmo.systembridge.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.buttonSetupBridge).setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.editTextName).text.toString()
            val host = findViewById<TextInputEditText>(R.id.editTextHost).text.toString()
            val apiPort =
                findViewById<TextInputEditText>(R.id.editTextApiPort).text.toString().toInt()
            val apiKey = findViewById<TextInputEditText>(R.id.editTextApiKey).text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                val connectionDao = AppDatabase.getInstance(applicationContext).connectionDao()
                connectionDao.insert(
                    Connection(
                        0,
                        name,
                        host,
                        apiPort,
                        apiKey
                    )
                )

                Log.d("SetupActivity", connectionDao.getAll().toString())

                launch(Dispatchers.Main) {
                    Log.d("SetupActivity", "finish()")
                    finish()
                }
            }
        }
    }
}
