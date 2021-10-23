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
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI
import java.net.URISyntaxException

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
        spinnerBridge = findViewById(R.id.spinnerBridge)
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

                val request = ServiceBuilder.buildService(
                    "http://${connection.host}:${connection.apiPort}",
                    Endpoints::class.java
                )
                val call = request.postOpen(connection.apiKey, Open(this.url))

                call.enqueue(object : Callback<Open> {
                    override fun onResponse(
                        call: Call<Open>,
                        response: Response<Open>,
                    ) {
                        Log.v(TAG, response.toString())

                        textviewResponse.setText(R.string.generic_success)
                        textviewResponse.setTextColor(resources.getColor(R.color.green_800, theme))

                        buttonOpen.visibility = VISIBLE
                        progressBarSending.visibility = INVISIBLE
                    }

                    override fun onFailure(call: Call<Open>, t: Throwable) {
                        val error = t.message.toString()
                        Log.e(TAG, error)

                        val message = "${getString(R.string.generic_error)}: $error"
                        textviewResponse.text = message
                        textviewResponse.setTextColor(resources.getColor(R.color.red_800, theme))

                        buttonOpen.visibility = VISIBLE
                        progressBarSending.visibility = INVISIBLE
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
                Log.d(TAG, "Set adapter")
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
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text: String ->
            val regex =
                "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
            val matchedText = regex.find(text, 0)
            Log.d(TAG, "matchedText: $matchedText")
            try {
                val url = URI(matchedText?.value).toString()
                this.url = url
                findViewById<TextView>(R.id.textViewUrl).text = url
            } catch (e: URISyntaxException) {
                val message = "${getText(R.string.url_error)}: $text"
                Log.e(TAG, "$message - $e")
                Toast.makeText(this, message, LENGTH_LONG).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "OpenInActivity"
    }

}
