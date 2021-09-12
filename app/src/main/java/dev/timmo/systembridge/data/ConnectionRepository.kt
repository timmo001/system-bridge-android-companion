package dev.timmo.systembridge.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(private val ConnectionDao: ConnectionDao) {

    fun getConnections() = ConnectionDao.getAll()

    fun getConnection(host: String) = ConnectionDao.findByHost(host)
}
