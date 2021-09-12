package dev.timmo.systembridge

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WebviewActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
//        val webviewMain = findViewById<WebView>(R.id.webviewMain).also { webview: WebView ->
//            webview.settings.javaScriptEnabled = true
//        }
//
//        val url = Uri.Builder()
//            .scheme("http")
//            .authority("${connection.host}:${connection.apiPort}")
//            .appendPath("app")
//            .appendPath("settings")
//            .appendQueryParameter("apiHost", connection.host)
//            .appendQueryParameter("apiKey", connection.apiKey)
//            .appendQueryParameter("apiPort", connection.apiPort.toString())
//            .appendQueryParameter("wsPort", wsPort.toString())
//            .build()
//            .toString()
//
//        Toast.makeText(this, "URL: $url", Toast.LENGTH_LONG).show()
//
//        Log.v("MainActivity", "URL: $url")
//
//        webviewMain.loadUrl(url)
    }
}
