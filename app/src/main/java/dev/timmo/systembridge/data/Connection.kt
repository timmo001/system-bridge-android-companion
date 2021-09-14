package dev.timmo.systembridge.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.timmo.systembridge.Constants.CONNECTION_API_KEY
import dev.timmo.systembridge.Constants.CONNECTION_API_PORT
import dev.timmo.systembridge.Constants.CONNECTION_HOST
import dev.timmo.systembridge.Constants.CONNECTION_MAC_ADDRESS
import dev.timmo.systembridge.Constants.CONNECTION_NAME

@Entity
data class Connection(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = CONNECTION_NAME) val name: String,
    @ColumnInfo(name = CONNECTION_MAC_ADDRESS) var macAddress: String,
    @ColumnInfo(name = CONNECTION_HOST) val host: String,
    @ColumnInfo(name = CONNECTION_API_PORT) val apiPort: Int,
    @ColumnInfo(name = CONNECTION_API_KEY) val apiKey: String,
)
