package dev.timmo.systembridge.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.view.BridgesRecyclerViewAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@DelicateCoroutinesApi
class OpenInActivity : AppCompatActivity() {

    private lateinit var buttonOpen: Button
    private lateinit var spinnerBridge: Spinner

    private lateinit var connectionData: List<Connection>
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_in)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type)
                    handleSendText(intent)
            }
        }

        buttonOpen = findViewById<Button>(R.id.buttonOpen).also { button: Button ->
            button.isEnabled = false
        }
        spinnerBridge = findViewById<Spinner>(R.id.spinnerBridge)
        val progressBarSending = findViewById<ProgressBar>(R.id.progressBarSending)
        val textviewResponse = findViewById<TextView>(R.id.textViewResponse)

        val textViewResponseOriginalColor = textviewResponse.textColors

        getData()

        buttonOpen.setOnClickListener {
            textviewResponse.text = null
            textviewResponse.setTextColor(textViewResponseOriginalColor)

            buttonOpen.visibility = INVISIBLE
            progressBarSending.visibility = VISIBLE

            val connection = connectionData.find { connection: Connection ->
                connection.name == spinnerBridge.selectedItem
            }

            if (connection !== null) {
                val objRequest = HashMap<String, String>()
                objRequest["path"] = this.url

                val queue = Volley.newRequestQueue(this)
                val request = object : JsonObjectRequest(
                    Method.POST,
                    "http://${connection.host}:${connection.apiPort}/open",
                    JSONObject(objRequest as Map<*, *>),
                    { response ->
                        Log.v("OpenInActivity", response.toString())

                        textviewResponse.setText(R.string.generic_success)
                        textviewResponse.setTextColor(resources.getColor(R.color.green_800, theme))

                        buttonOpen.visibility = VISIBLE
                        progressBarSending.visibility = INVISIBLE
                    },
                    { error ->
                        Log.e("OpenInActivity", error.toString())

                        val message = "${getString(R.string.generic_error)}: $error"
                        textviewResponse.text = message
                        textviewResponse.setTextColor(resources.getColor(R.color.red_800, theme))
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
        }

    }

    private fun getData() {
        val context = this

        GlobalScope.launch(Dispatchers.IO) {
            connectionData =
                AppDatabase.getInstance(applicationContext).connectionDao().getAll()

            Log.d("OpenInActivity", connectionData.toString())

            launch(Dispatchers.Main) {
                Log.d("OpenInActivity", "Set adapter")
                spinnerBridge.adapter =
                    ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        connectionData.map { connection: Connection ->
                            connection.name
                        }
                    )
                buttonOpen.isEnabled = true
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
            // Update UI to reflect text being shared
            this.url = url
            findViewById<TextView>(R.id.textViewUrl).text = url
        }
    }

}
