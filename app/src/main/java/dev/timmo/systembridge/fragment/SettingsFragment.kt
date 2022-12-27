package dev.timmo.systembridge.fragment

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.timmo.systembridge.activity.EditConnectionActivity
import dev.timmo.systembridge.data.AppDatabase
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.databinding.FragmentSettingsBinding
import dev.timmo.systembridge.shared.Constants
import dev.timmo.systembridge.view.BridgesRecyclerViewAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress

@OptIn(DelicateCoroutinesApi::class)
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    private lateinit var connectionData: List<Connection>
    private lateinit var connectionDiscoveredData: List<Connection>
    private lateinit var nsdManager: NsdManager
    private lateinit var fragmentContext: Context

    private lateinit var recyclerViewBridges: RecyclerView
    private lateinit var recyclerViewDiscoveredBridges: RecyclerView
    private lateinit var textViewSettingsDiscoveredBridges: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentContext = requireContext()
        connectionData = emptyList()
        connectionDiscoveredData = emptyList()

        recyclerViewBridges =
            binding.recyclerViewBridges.also { recyclerView: RecyclerView ->
                recyclerView.layoutManager = GridLayoutManager(fragmentContext, 1)
                recyclerView.adapter = BridgesRecyclerViewAdapter(
                    false,
                    connectionData,
                    this@SettingsFragment::onClickListener
                )
            }
        recyclerViewDiscoveredBridges =
            binding.recyclerViewDiscoveredBridges.also { recyclerView: RecyclerView ->
                recyclerView.layoutManager = GridLayoutManager(fragmentContext, 1)
                recyclerView.adapter = BridgesRecyclerViewAdapter(
                    true,
                    connectionDiscoveredData,
                    this@SettingsFragment::onClickListener
                )
            }
        textViewSettingsDiscoveredBridges = binding.textViewSettingsDiscoveredBridges

        binding.buttonAddNewBridge.setOnClickListener {
            startActivity(Intent(fragmentContext, EditConnectionActivity::class.java))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onPause() {
        super.onPause()
        connectionDiscoveredData = emptyList()
        nsdManager.stopServiceDiscovery(discoveryListener)
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        GlobalScope.launch(Dispatchers.IO) {
            connectionData = AppDatabase.getInstance(fragmentContext).connectionDao().getAll()

            Log.d(TAG, "connectionData: $connectionData")

            launch(Dispatchers.Main) {
                Log.d(TAG, "Set adapter")
                recyclerViewBridges.adapter = BridgesRecyclerViewAdapter(
                    false,
                    connectionData,
                    this@SettingsFragment::onClickListener,
                )
                textViewSettingsDiscoveredBridges.visibility = View.INVISIBLE
                connectionDiscoveredData = emptyList()
                nsdManager = (fragmentContext.getSystemService(Context.NSD_SERVICE) as NsdManager)
                nsdManager.discoverServices(
                    SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD,
                    discoveryListener
                )
            }
        }
    }

    private fun onClickListener(discovered: Boolean, position: Int) {
        val intentSetupActivity = Intent(fragmentContext, EditConnectionActivity::class.java)
        if (discovered) {
            intentSetupActivity.putExtra(Constants.CONNECTION_UID,
                connectionDiscoveredData[position].uid)
            intentSetupActivity.putExtra(Constants.CONNECTION_NAME,
                connectionDiscoveredData[position].name)
            intentSetupActivity.putExtra(Constants.CONNECTION_HOST,
                connectionDiscoveredData[position].host)
            intentSetupActivity.putExtra(Constants.CONNECTION_API_PORT,
                connectionDiscoveredData[position].apiPort)
            intentSetupActivity.putExtra(Constants.CONNECTION_API_KEY,
                connectionDiscoveredData[position].apiKey)
        } else {
            intentSetupActivity.putExtra(Constants.SETUP_EDIT, true)
            intentSetupActivity.putExtra(Constants.CONNECTION_UID, connectionData[position].uid)
            intentSetupActivity.putExtra(Constants.CONNECTION_NAME, connectionData[position].name)
            intentSetupActivity.putExtra(Constants.CONNECTION_HOST, connectionData[position].host)
            intentSetupActivity.putExtra(Constants.CONNECTION_API_PORT,
                connectionData[position].apiPort)
            intentSetupActivity.putExtra(Constants.CONNECTION_API_KEY,
                connectionData[position].apiKey)
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
            val uuid: String = serviceInfo.attributes["uuid"]?.decodeToString() ?: ""

            Log.d(TAG, "uuid: $uuid")

            val foundConnection =
                connectionData.find { connection: Connection ->
                    connection.uuid == uuid
                }

            Log.d(TAG, "foundConnection: $foundConnection")

            val foundDiscoveredConnection =
                connectionDiscoveredData.find { connection: Connection ->
                    connection.uuid == uuid
                }

            Log.d(TAG, "foundDiscoveredConnection: $foundDiscoveredConnection")

            var show = false
            if (foundConnection == null && foundDiscoveredConnection == null) {
                if (connectionDiscoveredData.isEmpty()) show = true
                connectionDiscoveredData = connectionDiscoveredData +
                        Connection(0, host.hostName, uuid, host.hostName, port, "")
            }

            Log.d(TAG, "connectionDiscoveredData: $connectionDiscoveredData")

            GlobalScope.launch(Dispatchers.Main) {
                if (show) textViewSettingsDiscoveredBridges.visibility = View.VISIBLE
                Log.d(TAG, "Set discovered adapter")
                recyclerViewDiscoveredBridges.adapter = BridgesRecyclerViewAdapter(
                    true,
                    connectionDiscoveredData,
                    this@SettingsFragment::onClickListener,
                )
            }

        }
    }

    companion object {
        private const val TAG = "SettingsActivity"
        private const val SERVICE_TYPE = "_system-bridge._tcp."
        private const val SERVICE_PREFIX = "System Bridge"
    }

}