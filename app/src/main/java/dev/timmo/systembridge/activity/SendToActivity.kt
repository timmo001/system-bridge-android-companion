package dev.timmo.systembridge.activity

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import java.io.File

@DelicateCoroutinesApi
class SendToActivity : AppCompatActivity() {

    private lateinit var buttonSend: Button
    private lateinit var spinnerBridge: Spinner
    private lateinit var spinnerPathBase: Spinner

    private lateinit var connectionData: List<Connection>
    private lateinit var pathBaseItems: List<String>

    private lateinit var filename: String
    private lateinit var mimeType: String
    private lateinit var path: String
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_to)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                handleFile(intent)
            }
        }

        pathBaseItems = resources.getStringArray(R.array.path_base).toList()

        val textViewPath = findViewById<TextView>(R.id.textViewPath)

        buttonSend = findViewById<Button>(R.id.buttonSend).also { button: Button ->
            button.isEnabled = false
        }
        spinnerBridge = findViewById(R.id.spinnerBridge)
        spinnerPathBase = findViewById<Spinner>(R.id.spinnerPathBase).also { spinner: Spinner ->
            spinner.adapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    pathBaseItems
                )
            // Select downloads by default
            spinner.setSelection(2)
            path = "${pathBaseItems[2]}/$filename"
            textViewPath.text = path

            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    this@SendToActivity.path = "${pathBaseItems[position]}/$filename"
                    textViewPath.text = path
                }

            }
        }
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
                val inputStream = contentResolver.openInputStream(this.uri)
                val imageData = inputStream?.readBytes()


                val objRequest = HashMap<String, String>()
                objRequest["path"] = this.path

                Log.d(TAG, "path: ${objRequest["path"]}")

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
                        val params = HashMap<String, FileDataPart>()
                        params["imageFile"] = FileDataPart(filename, imageData!!, mimeType)
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

    private fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun Context.getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                .let(cursor::getString)
        }
    }.getOrNull()

    private fun handleFile(intent: Intent) {
        Log.d(TAG, "intent: $intent")
        Log.d(TAG, "mimeType: ${intent.type}")
        this.mimeType = intent.type.toString()

        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri: Uri ->
            Log.d(TAG, "uri: $uri")
            this.uri = uri

            this.filename = getFileName(uri).orEmpty()
            Log.d(TAG, "filename: ${this.filename}")
        }
    }

    companion object {
        private const val TAG = "SendToActivity"
    }

}