package com.jnetaol.sshcommander.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jnetaol.sshcommander.data.model.SSHConnection
import com.jnetaol.sshcommander.data.model.SavedCommand
import com.jnetaol.sshcommander.logger.DebugLogger

@Database(entities = [SSHConnection::class, SavedCommand::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun commandDao(): CommandDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): AppDatabase = try {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "sshcommander.db")
                .fallbackToDestructiveMigration().build()
        } catch (e: Exception) {
            DebugLogger.e("AppDatabase", "DB creation failed", "SC-DB-001", e)
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "sshcommander_fallback.db")
                .fallbackToDestructiveMigration().build()
        }
    }
}
