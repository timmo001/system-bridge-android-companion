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
import dev.timmo.systembridge.view.BridgesRecyclerViewAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
@SuppressLint("NotifyDataSetChanged")
class SettingsActivity : AppCompatActivity() {
    private lateinit var connectionData: List<Connection>

    private lateinit var recyclerViewBridges: RecyclerView
    private lateinit var bridgesAdapter: BridgesRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        recyclerViewBridges = findViewById<RecyclerView>(R.id.recyclerViewBridges).also { recyclerView: RecyclerView ->
            recyclerView.layoutManager = GridLayoutManager(this, 1)
        }

        getData()

        findViewById<Button>(R.id.buttonAddNewBridge).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        GlobalScope.launch(Dispatchers.IO) {
            connectionData =
                AppDatabase.getInstance(applicationContext).connectionDao().getAll()

            Log.d("SettingsActivity", connectionData.toString())

            launch(Dispatchers.Main) {
                Log.d("SettingsActivity", "Set adapter")
                bridgesAdapter = BridgesRecyclerViewAdapter(connectionData)
                recyclerViewBridges.adapter = bridgesAdapter
            }
        }
    }

}
