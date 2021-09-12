package dev.timmo.systembridge.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Connection::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
}
