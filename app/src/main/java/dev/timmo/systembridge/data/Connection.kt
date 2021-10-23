package dev.timmo.systembridge.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.timmo.systembridge.shared.Constants.CONNECTION_API_KEY
import dev.timmo.systembridge.shared.Constants.CONNECTION_API_PORT
import dev.timmo.systembridge.shared.Constants.CONNECTION_HOST
import dev.timmo.systembridge.shared.Constants.CONNECTION_NAME
import dev.timmo.systembridge.shared.Constants.CONNECTION_UUID

@Entity
data class Connection(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = CONNECTION_NAME) val name: String,
    @ColumnInfo(name = CONNECTION_UUID) var uuid: String,
    @ColumnInfo(name = CONNECTION_HOST) val host: String,
    @ColumnInfo(name = CONNECTION_API_PORT) val apiPort: Int,
    @ColumnInfo(name = CONNECTION_API_KEY) val apiKey: String,
)
