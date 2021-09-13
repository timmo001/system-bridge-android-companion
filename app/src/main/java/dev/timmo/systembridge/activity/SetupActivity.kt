package dev.timmo.systembridge.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dev.timmo.systembridge.Constants.Companion.CONNECTION_API_KEY
import dev.timmo.systembridge.Constants.Companion.CONNECTION_API_PORT
import dev.timmo.systembridge.Constants.Companion.CONNECTION_HOST
import dev.timmo.systembridge.Constants.Companion.CONNECTION_NAME
import dev.timmo.systembridge.Constants.Companion.SETUP_EDIT
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

        val editTextName = findViewById<TextInputEditText>(R.id.editTextName)
        val editTextHost = findViewById<TextInputEditText>(R.id.editTextHost)
        val editTextApiPort = findViewById<TextInputEditText>(R.id.editTextApiPort)
        val editTextApiKey = findViewById<TextInputEditText>(R.id.editTextApiKey)

        if(intent?.getBooleanExtra(SETUP_EDIT,false) == true){
            editTextName.setText(intent.getStringExtra(CONNECTION_NAME))
            editTextHost.setText(intent.getStringExtra(CONNECTION_HOST))
            editTextApiPort.setText(intent.getStringExtra(CONNECTION_API_PORT))
            editTextApiKey.setText(intent.getStringExtra(CONNECTION_API_KEY))
        }

        findViewById<Button>(R.id.buttonSetupBridge).setOnClickListener {
            val name = editTextName.text.toString()
            val host = editTextHost.text.toString()
            val apiPort = editTextApiPort.text.toString().toInt()
            val apiKey = editTextApiKey.text.toString()

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
