package dev.timmo.systembridge.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import dev.timmo.systembridge.R
import dev.timmo.systembridge.data.Connection

class BridgesRecyclerViewAdapter(
    private val discovered: Boolean,
    private val connections: List<Connection>,
    private val onClickListener: (Boolean, Int) -> Unit,
) :
    RecyclerView.Adapter<BridgesRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.textViewName)
        val textViewHost: TextView = view.findViewById(R.id.textViewHost)
        val constraintLayoutBridge: ConstraintLayout =
            view.findViewById(R.id.constraintLayoutBridge)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_bridge_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val connection: Connection = connections[position]
        val url = "http://${connection.host}:${connection.apiPort}"
        viewHolder.textViewName.apply {
            this.text = connection.name
            if (discovered) this.setTextColor(
                resources.getColor(
                    R.color.green_800,
                    resources.newTheme()
                ))
        }
        viewHolder.textViewHost.apply {
            this.text = url
            if (discovered) this.setTextColor(
                resources.getColor(
                    R.color.green_800,
                    resources.newTheme()
                ))
        }
        viewHolder.constraintLayoutBridge.setOnClickListener {
            onClickListener(discovered, position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = connections.size

}
