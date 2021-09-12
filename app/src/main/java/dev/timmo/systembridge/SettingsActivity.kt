package dev.timmo.systembridge

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.view.BridgesRecyclerViewAdapter
import dev.timmo.systembridge.view.ConnectionViewModel

@SuppressLint("NotifyDataSetChanged")
class SettingsActivity : AppCompatActivity() {
    private lateinit var connectionData: List<Connection>
    private lateinit var bridgesAdapter: BridgesRecyclerViewAdapter

    // Use the 'by viewModels()' Kotlin property delegate
    // from the activity-ktx artifact
    private val model: ConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<RecyclerView>(R.id.recyclerViewBridges).also { recyclerView: RecyclerView ->
            connectionData = emptyList()
            bridgesAdapter = BridgesRecyclerViewAdapter(connectionData)
            recyclerView.adapter = bridgesAdapter

            // Create the observer which updates the UI.
            val observer = Observer { connections: List<Connection> ->
                // Update the UI
                connectionData = connections
            }

            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            model.connections.observe(this, observer)


//            GlobalScope.launch(Dispatchers.IO) {
//                connectionData =
//                    AppDatabase.getInstance(applicationContext).connectionDao().getAll()
//
//                launch(Dispatchers.Main) {
//                    bridgesAdapter.notifyDataSetChanged()
//                }
//            }
        }

        findViewById<Button>(R.id.buttonAddNewBridge).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    override fun onResume() {
//        super.onResume()
//        connectionData = AppDatabase.getInstance(applicationContext).connectionDao().getAll()
//        bridgesAdapter.notifyDataSetChanged()
//    }
}
