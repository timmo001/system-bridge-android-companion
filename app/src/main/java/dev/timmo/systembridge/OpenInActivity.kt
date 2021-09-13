package dev.timmo.systembridge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@DelicateCoroutinesApi
class OpenInActivity : AppCompatActivity() {

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

        val spinnerBridge = findViewById<Spinner>(R.id.spinnerBridge)
        val buttonOpen = findViewById<Button>(R.id.buttonOpen).also { button: Button ->
            button.isEnabled = false
        }

        val context = this

        GlobalScope.launch(Dispatchers.IO) {
            connectionData =
                AppDatabase.getInstance(applicationContext).connectionDao().getAll()

            Log.d("SettingsActivity", connectionData.toString())

            launch(Dispatchers.Main) {
                Log.d("SettingsActivity", "Set adapter")
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

        buttonOpen.setOnClickListener {
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
                        Log.v("OpenIn", response.toString())
                    },
                    { error ->
                        Log.e("OpenIn", error.message.toString())
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

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
            // Update UI to reflect text being shared
            this.url = url
            findViewById<TextView>(R.id.textViewUrl).text = url
        }
    }

}
