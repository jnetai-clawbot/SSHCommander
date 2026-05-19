package com.jnetaol.sshcommander.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class SSHConnection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String = "My Server",
    @ColumnInfo(name = "host") val host: String = "",
    @ColumnInfo(name = "port") val port: Int = 22,
    @ColumnInfo(name = "username") val username: String = "root",
    @ColumnInfo(name = "password") val password: String = "",
    @ColumnInfo(name = "auth_type") val authType: String = "password",
    @ColumnInfo(name = "private_key_path") val privateKeyPath: String = "",
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_connected") val lastConnected: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "color_index") val colorIndex: Int = 0
)

@Entity(tableName = "saved_commands")
data class SavedCommand(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "connection_id") val connectionId: Long = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "command") val command: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_used") val lastUsed: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
