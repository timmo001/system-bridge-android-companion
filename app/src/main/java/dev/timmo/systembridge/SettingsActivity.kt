package dev.timmo.systembridge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.buttonAddNewBridge).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }
}
