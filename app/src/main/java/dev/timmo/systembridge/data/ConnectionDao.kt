package dev.timmo.systembridge.data

import androidx.room.*

@Dao
interface ConnectionDao {

    @Delete
    fun delete(user: Connection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg connection: Connection)

    @Query("SELECT * FROM connection")
    fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Connection>

    @Query("SELECT * FROM connection WHERE host = :host")
    fun findByHost(host: String): Connection

}
