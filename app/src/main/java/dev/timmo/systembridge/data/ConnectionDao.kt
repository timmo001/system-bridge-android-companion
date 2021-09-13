package dev.timmo.systembridge.data

import androidx.room.*

@Dao
interface ConnectionDao {

    @Delete
    fun delete(user: Connection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg connection: Connection)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg connection: Connection)

    @Query("SELECT * FROM connection")
    fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE name = :name")
    fun findByName(name: String): Connection

    @Query("SELECT * FROM connection WHERE host = :host")
    fun findByHost(host: String): Connection

}
