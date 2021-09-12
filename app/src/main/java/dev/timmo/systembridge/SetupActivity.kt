package dev.timmo.systembridge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.buttonSetupBridge).setOnClickListener {
            finish()
        }
    }
}