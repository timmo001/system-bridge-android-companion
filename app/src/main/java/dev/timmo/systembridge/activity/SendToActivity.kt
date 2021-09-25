package dev.timmo.systembridge.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.shared.FileDataPart
import dev.timmo.systembridge.shared.VolleyFileUploadRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class SendToActivity : AppCompatActivity() {

    private lateinit var buttonSend: Button
    private lateinit var spinnerBridge: Spinner
    private lateinit var spinnerPathBase: Spinner

    private lateinit var connectionData: List<Connection>
    private lateinit var text: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_to)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                handleFile(intent)
            }
        }

        buttonSend = findViewById<Button>(R.id.buttonSend).also { button: Button ->
            button.isEnabled = false
        }
        spinnerBridge = findViewById(R.id.spinnerBridge)
        spinnerPathBase = findViewById(R.id.spinnerPathBase)
        val progressBarSending = findViewById<ProgressBar>(R.id.progressBarSending)
        val textviewResponse = findViewById<TextView>(R.id.textViewResponse)

        val textViewResponseOriginalColor = textviewResponse.textColors

        getData()

        buttonSend.setOnClickListener {
            textviewResponse.text = null
            textviewResponse.setTextColor(textViewResponseOriginalColor)

            buttonSend.visibility = View.INVISIBLE
            progressBarSending.visibility = View.VISIBLE

            val connection = connectionData.find { connection: Connection ->
                connection.name == spinnerBridge.selectedItem
            }

            if (connection !== null) {
                val objRequest = HashMap<String, String>()
                objRequest["path"] = this.text

                val queue = Volley.newRequestQueue(this)
                val request = object : VolleyFileUploadRequest(
                    Method.POST,
                    "http://${connection.host}:${connection.apiPort}/filesystem/files/file",
                    { response ->
                        Log.v(TAG, response.toString())

                        textviewResponse.setText(R.string.generic_success)
                        textviewResponse.setTextColor(resources.getColor(R.color.green_800, theme))

                        buttonSend.visibility = View.VISIBLE
                        progressBarSending.visibility = View.INVISIBLE
                    },
                    { error ->
                        Log.e(TAG, error.toString())

                        val message = "${getString(R.string.generic_error)}: $error"
                        textviewResponse.text = message
                        textviewResponse.setTextColor(resources.getColor(R.color.red_800, theme))

                        buttonSend.visibility = View.VISIBLE
                        progressBarSending.visibility = View.INVISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["api-key"] = connection.apiKey
                        return headers
                    }

                    override fun getByteData(): MutableMap<String, FileDataPart> {
                        var params = HashMap<String, FileDataPart>()
                        params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                        return params
                    }
                }

                // Add the request to the RequestQueue.
                queue.add(request)
            }
        }

    }

    private fun getData() {
        val context = this

        GlobalScope.launch(Dispatchers.IO) {
            connectionData =
                AppDatabase.getInstance(applicationContext).connectionDao().getAll()

            Log.d(TAG, connectionData.toString())

            launch(Dispatchers.Main) {
                Log.d(TAG, "Set adapter")
                spinnerBridge.adapter =
                    ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        connectionData.map { connection: Connection ->
                            connection.name
                        }
                    )
                buttonSend.isEnabled = true
            }
        }
    }

    private fun handleFile(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text: String ->
            Log.d(TAG, "file: $text")
            this.text = text
        }
    }

    companion object {
        private const val TAG = "SendToActivity"
    }


}