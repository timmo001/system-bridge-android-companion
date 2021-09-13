package dev.timmo.systembridge.activity

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dev.timmo.systembridge.Constants.Companion.CONNECTION_API_KEY
import dev.timmo.systembridge.Constants.Companion.CONNECTION_API_PORT
import dev.timmo.systembridge.Constants.Companion.CONNECTION_HOST
import dev.timmo.systembridge.Constants.Companion.CONNECTION_NAME
import dev.timmo.systembridge.Constants.Companion.CONNECTION_UID
import dev.timmo.systembridge.Constants.Companion.DEFAULT_API_PORT
import dev.timmo.systembridge.Constants.Companion.SETUP_EDIT
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class EditConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_connection)

        val editTextName = findViewById<TextInputEditText>(R.id.editTextName)
        val editTextHost = findViewById<TextInputEditText>(R.id.editTextHost)
        val editTextApiPort = findViewById<TextInputEditText>(R.id.editTextApiPort)
        val editTextApiKey = findViewById<TextInputEditText>(R.id.editTextApiKey)
        val buttonSetupBridge = findViewById<Button>(R.id.buttonSetupBridge)
        val buttonDeleteBridge = findViewById<Button>(R.id.buttonDeleteBridge)

        val edit = intent?.getBooleanExtra(SETUP_EDIT, false) == true
        val uid = intent?.getIntExtra(CONNECTION_UID, 0) ?: 0

        if (edit) {
            findViewById<TextView>(R.id.textViewSetupBridge).setText(R.string.edit_bridge)
            buttonSetupBridge.setText(R.string.save)
            editTextName.setText(intent.getStringExtra(CONNECTION_NAME))
            editTextHost.setText(intent.getStringExtra(CONNECTION_HOST))
            editTextApiPort.setText(
                intent.getIntExtra(CONNECTION_API_PORT, DEFAULT_API_PORT).toString()
            )
            editTextApiKey.setText(intent.getStringExtra(CONNECTION_API_KEY))
        } else {
            buttonDeleteBridge.visibility = GONE
            editTextApiPort.text =
                Editable.Factory.getInstance().newEditable(DEFAULT_API_PORT.toString())
        }

        buttonDeleteBridge.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val connection = Connection(uid, "", "", 0, "")

                Log.d("SetupActivity", connection.toString())

                val connectionDao = AppDatabase.getInstance(applicationContext).connectionDao()
                connectionDao.delete(connection)

                launch(Dispatchers.Main) {
                    finish()
                }
            }
        }

        buttonSetupBridge.setOnClickListener {
            val name = editTextName.text.toString()
            val host = editTextHost.text.toString()
            val apiPort = editTextApiPort.text.toString().toInt()
            val apiKey = editTextApiKey.text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                val connection = Connection(uid, name, host, apiPort, apiKey)

                Log.d("SetupActivity", connection.toString())

                val connectionDao = AppDatabase.getInstance(applicationContext).connectionDao()
                if (edit) connectionDao.update(connection)
                else connectionDao.insert(connection)

                launch(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
}
