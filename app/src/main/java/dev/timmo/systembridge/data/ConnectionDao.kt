package dev.timmo.systembridge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Connection>

    @Query("SELECT * FROM connection WHERE host = :host")
    fun findByHost(host: String): Connection

    @Insert
    fun insertAll(vararg users: Connection)

    @Delete
    fun delete(user: Connection)

}