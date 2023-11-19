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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.timmo.systembridge.shared.Constants.CONNECTION_TOKEN
import dev.timmo.systembridge.shared.Constants.CONNECTION_API_PORT
import dev.timmo.systembridge.shared.Constants.CONNECTION_HOST
import dev.timmo.systembridge.shared.Constants.CONNECTION_NAME
import dev.timmo.systembridge.shared.Constants.CONNECTION_UID
import dev.timmo.systembridge.shared.Constants.DEFAULT_API_PORT
import dev.timmo.systembridge.shared.Constants.SETUP_EDIT
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.data.bridge.Endpoints
import dev.timmo.systembridge.data.bridge.SystemBridgeSystem
import dev.timmo.systembridge.shared.ServiceBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class EditConnectionActivity : AppCompatActivity() {
    private lateinit var buttonSave: Button
    private lateinit var editTextToken: TextInputEditText
    private lateinit var editTextTokenLayout: TextInputLayout
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
        editTextToken = findViewById(R.id.editTextToken)
        editTextTokenLayout = findViewById(R.id.editTextTokenLayout)
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

        editTextName.setText(intent.getStringExtra(CONNECTION_NAME))
        editTextHost.setText(intent.getStringExtra(CONNECTION_HOST))
        editTextApiPort.setText(
            intent.getIntExtra(CONNECTION_API_PORT, DEFAULT_API_PORT).toString()
        )

        if (edit) {
            findViewById<TextView>(R.id.textViewSetupBridge).setText(R.string.edit_bridge)
            buttonSave.setText(R.string.save)
            editTextToken.setText(intent.getStringExtra(CONNECTION_TOKEN))
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
            val token = editTextToken.text.toString()

            buttonSave.visibility = INVISIBLE
            progressBarSaving.visibility = VISIBLE
            textViewTestConnection.setText(R.string.test_connection_in_progress)

            val connection = Connection(uid, name, "", host, apiPort, token)
            Log.d(TAG, connection.toString())

            testConnection(edit, connection)
        }

        // Validation
        editTextName.addTextChangedListener { validateInput() }
        editTextHost.addTextChangedListener { validateInput() }
        editTextApiPort.addTextChangedListener { validateInput() }
        editTextToken.addTextChangedListener { validateInput() }

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
        if (editTextToken.text.isNullOrBlank()) {
            editTextTokenLayout.error =
                "${getString(R.string.validation_error_an)} ${getString(R.string.token)}"
            saveEnabled = false
        } else editTextTokenLayout.error = null

        buttonSave.isEnabled = saveEnabled
    }

    private fun testConnection(edit: Boolean, connection: Connection) {
        textViewTestConnection.text = null
        textViewTestConnection.setTextColor(textViewTestConnectionOriginalColor)

        val request = ServiceBuilder.buildService(
            "http://${connection.host}:${connection.apiPort}",
            Endpoints::class.java
        )
        val call = request.getSystem(connection.token)

        call.enqueue(object : Callback<SystemBridgeSystem> {
            override fun onResponse(call: Call<SystemBridgeSystem>, response: Response<SystemBridgeSystem>) {
                Log.d(TAG, response.toString())

                textViewTestConnection.setText(R.string.generic_success)
                textViewTestConnection.setTextColor(resources.getColor(R.color.green_800, theme))

                connection.uuid = response.body()?.uuid ?: ""

                Log.d(TAG, "uuid: ${connection.uuid}")

                updateItem(edit, connection)

                buttonSave.visibility = VISIBLE
                progressBarSaving.visibility = INVISIBLE
            }

            override fun onFailure(call: Call<SystemBridgeSystem>, t: Throwable) {
                val error = t.message.toString()
                Log.e(TAG, error)

                val message = "${getString(R.string.test_connection_error)}: $error"
                textViewTestConnection.text = message
                textViewTestConnection.setTextColor(resources.getColor(R.color.red_800, theme))

                buttonSave.visibility = VISIBLE
                progressBarSaving.visibility = INVISIBLE
            }

        })
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
            val connection = Connection(uid, "", "", "", 0, "")

            Log.d(TAG, connection.toString())

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

    companion object {
        private const val TAG = "SetupActivity"
    }

}
