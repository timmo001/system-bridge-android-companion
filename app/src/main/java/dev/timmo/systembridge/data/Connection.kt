package dev.timmo.systembridge.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Connection(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "host") val host: String,
    @ColumnInfo(name = "api_port") val apiPort: Int,
    @ColumnInfo(name = "api_key") val apiKey: String,
)
