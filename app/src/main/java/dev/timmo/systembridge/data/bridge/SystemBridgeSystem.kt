package dev.timmo.systembridge.data.bridge

data class SystemBridgeSystem(
    val boot_time: String,
    val fqdn: String,
    val hostname: String,
    val ip_address_4: String,
    val mac_address: String,
    val platform: String,
    val platform_version: String,
    val uptime: String,
    val uuid: String,
    val version: String,
    val version_latest: String,
    val version_newer_available: Boolean,
)
