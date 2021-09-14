package dev.timmo.systembridge.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.timmo.systembridge.Constants.CONNECTION_API_KEY
import dev.timmo.systembridge.Constants.CONNECTION_API_PORT
import dev.timmo.systembridge.Constants.CONNECTION_HOST
import dev.timmo.systembridge.Constants.CONNECTION_NAME
import dev.timmo.systembridge.Constants.CONNECTION_UID
import dev.timmo.systembridge.Constants.DEFAULT_API_PORT
import dev.timmo.systembridge.Constants.SETUP_EDIT
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@DelicateCoroutinesApi
class EditConnectionActivity : AppCompatActivity() {
    private lateinit var buttonSave: Button
    private lateinit var editTextApiKey: TextInputEditText
    private lateinit var editTextApiKeyLayout: TextInputLayout
    private lateinit var editTextApiPort: TextInputEditText
    private lateinit var editTextApiPortLayout: TextInputLayout
    private lateinit var editTextHost: TextInputEditText
    private lateinit var editTextHostLayout: TextInputLayout
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextNameLayout: TextInputLayout
    private lateinit var progressBarSaving: ProgressBar
    private lateinit var textViewTestConnection: TextView
    private lateinit var textViewTestConnectionOriginalColor: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_connection)

        val buttonDeleteBridge = findViewById<Button>(R.id.buttonDeleteBridge)
        buttonSave = findViewById(R.id.buttonSetupBridge)
        editTextApiKey = findViewById(R.id.editTextApiKey)
        editTextApiKeyLayout = findViewById(R.id.editTextApiKeyLayout)
        editTextApiPort = findViewById(R.id.editTextApiPort)
        editTextApiPortLayout = findViewById(R.id.editTextApiPortLayout)
        editTextHost = findViewById(R.id.editTextHost)
        editTextHostLayout = findViewById(R.id.editTextHostLayout)
        editTextName = findViewById(R.id.editTextName)
        editTextNameLayout = findViewById(R.id.editTextNameLayout)
        progressBarSaving = findViewById(R.id.progressBarSaving)
        textViewTestConnection = findViewById(R.id.textViewTestConnection)

        textViewTestConnectionOriginalColor = textViewTestConnection.textColors

        val edit = intent?.getBooleanExtra(SETUP_EDIT, false) == true
        val uid = intent?.getIntExtra(CONNECTION_UID, 0) ?: 0

        if (edit) {
            findViewById<TextView>(R.id.textViewSetupBridge).setText(R.string.edit_bridge)
            buttonSave.setText(R.string.save)
            editTextName.setText(intent.getStringExtra(CONNECTION_NAME))
            editTextHost.setText(intent.getStringExtra(CONNECTION_HOST))
            editTextApiPort.setText(
                intent.getIntExtra(CONNECTION_API_PORT, DEFAULT_API_PORT).toString()
            )
            editTextApiKey.setText(intent.getStringExtra(CONNECTION_API_KEY))
        } else {
            buttonDeleteBridge.isEnabled = false
            buttonDeleteBridge.visibility = GONE
            editTextApiPort.text =
                Editable.Factory.getInstance().newEditable(DEFAULT_API_PORT.toString())
        }

        buttonDeleteBridge.setOnClickListener {
            deleteConfirmation(uid)
        }

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString()
            val host = editTextHost.text.toString()
            val apiPort = editTextApiPort.text.toString().toInt()
            val apiKey = editTextApiKey.text.toString()

            buttonSave.visibility = INVISIBLE
            progressBarSaving.visibility = VISIBLE
            textViewTestConnection.setText(R.string.test_connection_in_progress)

            val connection = Connection(uid, name, host, apiPort, apiKey)
            Log.d("SetupActivity", connection.toString())

            testConnection(edit, connection)
        }

        // Validation
        editTextName.addTextChangedListener { validateInput() }
        editTextHost.addTextChangedListener { validateInput() }
        editTextApiPort.addTextChangedListener { validateInput() }
        editTextApiKey.addTextChangedListener { validateInput() }

        validateInput()
    }

    private fun validateInput() {
        var saveEnabled = true
        if (editTextName.text.isNullOrBlank()) {
            editTextNameLayout.error =
                "${getString(R.string.validation_error_a)} ${getString(R.string.name)}"
            saveEnabled = false
        } else editTextNameLayout.error = null
        if (editTextHost.text.isNullOrBlank()) {
            editTextHostLayout.error =
                "${getString(R.string.validation_error_a)} ${getString(R.string.host)}"
            saveEnabled = false
        } else editTextHostLayout.error = null
        if (editTextApiPort.text.isNullOrBlank()) {
            editTextApiPortLayout.error =
                "${getString(R.string.validation_error_a)} ${getString(R.string.api_port)}"
            saveEnabled = false
        } else editTextApiPortLayout.error = null
        if (editTextApiKey.text.isNullOrBlank()) {
            editTextApiKeyLayout.error =
                "${getString(R.string.validation_error_an)} ${getString(R.string.api_key)}"
            saveEnabled = false
        } else editTextApiKeyLayout.error = null

        buttonSave.isEnabled = saveEnabled
    }

    private fun testConnection(edit: Boolean, connection: Connection) {
        textViewTestConnection.setTextColor(textViewTestConnectionOriginalColor)

        val queue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(
            Method.GET,
            "http://${connection.host}:${connection.apiPort}/information",
            null,
            { response: JSONObject ->
                Log.d("SetupActivity", response.toString())

                textViewTestConnection.setText(R.string.generic_success)
                textViewTestConnection.setTextColor(resources.getColor(R.color.green_800, theme))

                updateItem(edit, connection)

                buttonSave.visibility = VISIBLE
                progressBarSaving.visibility = INVISIBLE
            },
            { error: VolleyError ->
                Log.e("SetupActivity", error.toString())

                val message = "${getString(R.string.test_connection_error)}: $error"
                textViewTestConnection.text = message
                textViewTestConnection.setTextColor(resources.getColor(R.color.red_800, theme))

                buttonSave.visibility = VISIBLE
                progressBarSaving.visibility = INVISIBLE
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["api-key"] = connection.apiKey
                return headers
            }
        }

        // Add the request to the RequestQueue.
        queue.add(request)

    }

    private fun deleteConfirmation(uid: Int) {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(R.string.confirmation_title)
            .setMessage(R.string.edit_bridge_delete_confirmation)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteItem(uid)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun deleteItem(uid: Int) {
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

    private fun updateItem(edit: Boolean, connection: Connection) {
        GlobalScope.launch(Dispatchers.IO) {
            val connectionDao =
                AppDatabase.getInstance(applicationContext).connectionDao()
            if (edit) connectionDao.update(connection)
            else connectionDao.insert(connection)

            launch(Dispatchers.Main) {
                finish()
            }
        }
    }

}
