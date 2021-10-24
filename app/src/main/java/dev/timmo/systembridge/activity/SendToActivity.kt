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
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.data.bridge.Endpoints
import dev.timmo.systembridge.shared.ServiceBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Callback
import java.io.BufferedInputStream
import java.io.File

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part.Companion.create
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody


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
                lateinit var bytes: ByteArray

                inputStream?.buffered().use { bufferedInputStream: BufferedInputStream? ->
                    if (bufferedInputStream != null) bytes = bufferedInputStream.readBytes()
                }

                val reqFile: RequestBody = bytes.toRequestBody(this.mimeType.toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("upload", this.filename, reqFile)
                val name: RequestBody =
                    "upload_test".toRequestBody("text/plain".toMediaTypeOrNull())

                val request = ServiceBuilder.buildService(
                    "http://${connection.host}:${connection.apiPort}",
                    Endpoints::class.java
                )
                val call = request.postFile(
                    connection.apiKey,
                    this.path,
                    body,
                    name,
                )

                call.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        Log.v(TAG, response.toString())

                        textviewResponse.setText(R.string.generic_success)
                        textviewResponse.setTextColor(resources.getColor(R.color.green_800, theme))

                        buttonSend.visibility = View.VISIBLE
                        progressBarSending.visibility = View.INVISIBLE
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        val error = t.message.toString()
                        Log.e(TAG, error)

                        val message = "${getString(R.string.generic_error)}: $error"
                        textviewResponse.text = message
                        textviewResponse.setTextColor(resources.getColor(R.color.red_800, theme))

                        buttonSend.visibility = View.VISIBLE
                        progressBarSending.visibility = View.INVISIBLE
                    }
                })
                
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