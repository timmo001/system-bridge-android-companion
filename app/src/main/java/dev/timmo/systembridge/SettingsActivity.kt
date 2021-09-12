package dev.timmo.systembridge

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.views.BridgesRecyclerViewAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
@SuppressLint("NotifyDataSetChanged")
class SettingsActivity : AppCompatActivity() {
    private lateinit var connectionData: List<Connection>
    private lateinit var bridgesAdapter: BridgesRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<RecyclerView>(R.id.recyclerViewBridges).also { recyclerView: RecyclerView ->
            recyclerView.layoutManager = GridLayoutManager(this, 1)

            GlobalScope.launch(Dispatchers.IO) {
                connectionData =
                    AppDatabase.getInstance(applicationContext).connectionDao().getAll()

                Log.d("SettingsActivity", connectionData.toString())

                launch(Dispatchers.Main) {
                    Log.d("SettingsActivity", "Set adapter")
                    bridgesAdapter = BridgesRecyclerViewAdapter(connectionData)
                    recyclerView.adapter = bridgesAdapter
                }
            }
        }

        findViewById<Button>(R.id.buttonAddNewBridge).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

}
