package dev.timmo.systembridge.data

data class Information(
    val address: String,
    val apiPort: Int,
    val cli: Boolean,
    val container: Boolean,
    val fqdn: String,
    val host: String,
    val ip: String,
    val mac: String,
    val updates: Any,
    val uuid: String,
    val version: String,
    val websocketAddress: String,
    val websocketPort: Int,
)
