package dev.timmo.systembridge.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.timmo.systembridge.Constants.CONNECTION_API_KEY
import dev.timmo.systembridge.Constants.CONNECTION_API_PORT
import dev.timmo.systembridge.Constants.CONNECTION_HOST
import dev.timmo.systembridge.Constants.CONNECTION_NAME
import dev.timmo.systembridge.Constants.CONNECTION_UID
import dev.timmo.systembridge.Constants.SETUP_EDIT
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.view.BridgesRecyclerViewAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.net.InetAddress

@DelicateCoroutinesApi
@SuppressLint("NotifyDataSetChanged")
class SettingsActivity : AppCompatActivity() {

    private lateinit var connectionData: List<Connection>
    private lateinit var connectionDiscoveredData: List<Connection>
    private lateinit var nsdManager: NsdManager

    private lateinit var recyclerViewBridges: RecyclerView
    private lateinit var recyclerViewDiscoveredBridges: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        connectionData = emptyList()
        connectionDiscoveredData = emptyList()

        recyclerViewBridges =
            findViewById<RecyclerView>(R.id.recyclerViewBridges).also { recyclerView: RecyclerView ->
                recyclerView.layoutManager = GridLayoutManager(this, 1)
                recyclerView.adapter = BridgesRecyclerViewAdapter(
                    false,
                    connectionData,
                    this@SettingsActivity::onClickListener
                )
            }
        recyclerViewDiscoveredBridges =
            findViewById<RecyclerView>(R.id.recyclerViewDiscoveredBridges).also { recyclerView: RecyclerView ->
                recyclerView.layoutManager = GridLayoutManager(this, 1)
                recyclerView.adapter = BridgesRecyclerViewAdapter(
                    true,
                    connectionDiscoveredData,
                    this@SettingsActivity::onClickListener
                )
            }

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager)
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        findViewById<Button>(R.id.buttonAddNewBridge).setOnClickListener {
            startActivity(Intent(this, EditConnectionActivity::class.java))
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

            Log.d(TAG, "connectionData: $connectionData")

            launch(Dispatchers.Main) {
                Log.d(TAG, "Set adapter")
                recyclerViewBridges.adapter = BridgesRecyclerViewAdapter(
                    false,
                    connectionData,
                    this@SettingsActivity::onClickListener,
                )
            }
        }
    }

    private fun onClickListener(discovered: Boolean, position: Int) {
        val intentSetupActivity = Intent(this, EditConnectionActivity::class.java)
        if (discovered) {
            intentSetupActivity.putExtra(CONNECTION_UID, connectionDiscoveredData[position].uid)
            intentSetupActivity.putExtra(CONNECTION_NAME, connectionDiscoveredData[position].name)
            intentSetupActivity.putExtra(CONNECTION_HOST, connectionDiscoveredData[position].host)
            intentSetupActivity.putExtra(CONNECTION_API_PORT,
                connectionDiscoveredData[position].apiPort)
            intentSetupActivity.putExtra(CONNECTION_API_KEY,
                connectionDiscoveredData[position].apiKey)
        } else {
            intentSetupActivity.putExtra(SETUP_EDIT, true)
            intentSetupActivity.putExtra(CONNECTION_UID, connectionData[position].uid)
            intentSetupActivity.putExtra(CONNECTION_NAME, connectionData[position].name)
            intentSetupActivity.putExtra(CONNECTION_HOST, connectionData[position].host)
            intentSetupActivity.putExtra(CONNECTION_API_PORT, connectionData[position].apiPort)
            intentSetupActivity.putExtra(CONNECTION_API_KEY, connectionData[position].apiKey)
        }

        startActivity(intentSetupActivity)
    }

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success: $service")
            Log.d(TAG, "Name: ${service.serviceName}")
            when {
                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName.startsWith(SERVICE_PREFIX) -> nsdManager.resolveService(service,
                    resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Resolve Succeeded: $serviceInfo")

            val host: InetAddress = serviceInfo.host
            val port: Int = serviceInfo.port
            val macAddress: String = serviceInfo.attributes["mac"]?.decodeToString() ?: ""

            Log.d(TAG, "macAddress: $macAddress")

            val foundConnection =
                connectionData.find { connection: Connection ->
                    connection.macAddress == macAddress
                }

            Log.d(TAG, "foundConnection: $foundConnection")

            val foundDiscoveredConnection =
                connectionDiscoveredData.find { connection: Connection ->
                    connection.macAddress == macAddress
                }

            Log.d(TAG, "foundDiscoveredConnection: $foundDiscoveredConnection")

            var show = false
            if (foundConnection == null && foundDiscoveredConnection == null) {
                if (connectionDiscoveredData.isEmpty()) show = true
                connectionDiscoveredData = connectionDiscoveredData +
                        Connection(0, host.hostName, macAddress, host.hostName, port, "")
            }

            Log.d(TAG, "connectionDiscoveredData: $connectionDiscoveredData")

            GlobalScope.launch(Dispatchers.Main) {
                if (show) findViewById<TextView>(R.id.textViewSettingsDiscoveredBridges).apply {
                    this.visibility = VISIBLE
                }
                Log.d(TAG, "Set discovered adapter")
                recyclerViewDiscoveredBridges.adapter = BridgesRecyclerViewAdapter(
                    true,
                    connectionDiscoveredData,
                    this@SettingsActivity::onClickListener,
                )
            }

        }
    }


    companion object {
        private const val TAG = "SettingsActivity"
        private const val SERVICE_TYPE = "_system-bridge._udp."
        private const val SERVICE_PREFIX = "System Bridge"
    }

}
