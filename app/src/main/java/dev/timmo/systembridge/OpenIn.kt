package dev.timmo.systembridge

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class OpenIn : AppCompatActivity() {

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

        findViewById<Button>(R.id.buttonOpen).setOnClickListener {

            val objRequest = HashMap<String, String>()
            objRequest["path"] = this.url

            val queue = Volley.newRequestQueue(this)
            val request = object : JsonObjectRequest(
                Method.POST, "http://$hostname:$apiPort/open", JSONObject(objRequest as Map<*, *>),
                { response ->
                    Log.v("OpenIn", response.toString())
                },
                { error ->
                    Log.e("OpenIn", error.message.toString())
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["api-key"] = apiKey
                    return headers
                }
            }

            // Add the request to the RequestQueue.
            queue.add(request)


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