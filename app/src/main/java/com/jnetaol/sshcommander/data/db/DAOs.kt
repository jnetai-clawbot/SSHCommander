package com.jnetaol.sshcommander.data.db

import androidx.room.*
import com.jnetaol.sshcommander.data.model.SSHConnection
import com.jnetaol.sshcommander.data.model.SavedCommand

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY is_favorite DESC, last_connected DESC")
    suspend fun getAll(): List<SSHConnection>

    @Query("SELECT * FROM connections WHERE id = :id")
    suspend fun getById(id: Long): SSHConnection?

    @Query("SELECT * FROM connections WHERE host LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY last_connected DESC")
    suspend fun search(query: String): List<SSHConnection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: SSHConnection): Long

    @Update
    suspend fun update(connection: SSHConnection)

    @Query("UPDATE connections SET is_favorite = :fav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, fav: Boolean)

    @Query("UPDATE connections SET last_connected = :time WHERE id = :id")
    suspend fun updateLastConnected(id: Long, time: Long)

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM connections")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM connections")
    suspend fun getCount(): Int
}

@Dao
interface CommandDao {
    @Query("SELECT * FROM saved_commands WHERE connection_id = 0 OR connection_id = :connectionId ORDER BY is_favorite DESC, last_used DESC")
    suspend fun getForConnection(connectionId: Long): List<SavedCommand>

    @Query("SELECT * FROM saved_commands ORDER BY is_favorite DESC, last_used DESC")
    suspend fun getAll(): List<SavedCommand>

    @Query("SELECT * FROM saved_commands WHERE id = :id")
    suspend fun getById(id: Long): SavedCommand?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(command: SavedCommand): Long

    @Update
    suspend fun update(command: SavedCommand)

    @Query("UPDATE saved_commands SET last_used = :time WHERE id = :id")
    suspend fun updateLastUsed(id: Long, time: Long)

    @Query("UPDATE saved_commands SET is_favorite = :fav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM saved_commands WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM saved_commands WHERE connection_id = :connectionId")
    suspend fun deleteForConnection(connectionId: Long)
}
