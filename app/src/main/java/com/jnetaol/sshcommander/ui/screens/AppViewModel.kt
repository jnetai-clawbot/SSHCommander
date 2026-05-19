package com.jnetaol.sshcommander.ui.screens

import android.app.Application
import androidx.lifecycle.*
import com.jnetaol.sshcommander.data.db.AppDatabase
import com.jnetaol.sshcommander.data.model.SSHConnection
import com.jnetaol.sshcommander.data.model.SavedCommand
import com.jnetaol.sshcommander.engine.SSHManager
import com.jnetaol.sshcommander.engine.SystemStats
import com.jnetaol.sshcommander.engine.DockerContainer
import com.jnetaol.sshcommander.logger.DebugLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _connections = MutableStateFlow<List<SSHConnection>>(emptyList())
    val connections: StateFlow<List<SSHConnection>> = _connections.asStateFlow()

    private val _commands = MutableStateFlow<List<SavedCommand>>(emptyList())
    val commands: StateFlow<List<SavedCommand>> = _commands.asStateFlow()

    private val _currentConnection = MutableStateFlow<SSHConnection?>(null)
    val currentConnection: StateFlow<SSHConnection?> = _currentConnection.asStateFlow()

    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    private val _stats = MutableStateFlow(SystemStats())
    val stats: StateFlow<SystemStats> = _stats.asStateFlow()

    private val _dockerContainers = MutableStateFlow<List<DockerContainer>>(emptyList())
    val dockerContainers: StateFlow<List<DockerContainer>> = _dockerContainers.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val connectionState = SSHManager.connectionState

    init {
        DebugLogger.d("AppViewModel", "ViewModel init", "SC-VM-001")
        loadConnections()
        loadCommands()
    }

    fun loadConnections() {
        scope.launch {
            try { _connections.value = db.connectionDao().getAll() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load connections failed", "SC-VM-ERR-001", e) }
        }
    }

    fun loadCommands() {
        scope.launch {
            try { _commands.value = db.commandDao().getAll() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load commands failed", "SC-VM-ERR-002", e) }
        }
    }

    fun searchConnections(query: String) {
        scope.launch {
            try {
                _connections.value = if (query.isBlank()) db.connectionDao().getAll()
                else db.connectionDao().search(query)
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Search failed", "SC-VM-ERR-003", e) }
        }
    }

    fun saveConnection(connection: SSHConnection) {
        scope.launch {
            try {
                if (connection.id == 0L) db.connectionDao().insert(connection)
                else db.connectionDao().update(connection)
                loadConnections()
                showToast("Connection saved")
                DebugLogger.i("AppViewModel", "Connection saved: ${connection.name}", "SC-VM-002")
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Save connection failed", "SC-VM-ERR-004", e) }
        }
    }

    fun deleteConnection(connection: SSHConnection) {
        scope.launch {
            try {
                db.commandDao().deleteForConnection(connection.id)
                db.connectionDao().delete(connection.id)
                if (_currentConnection.value?.id == connection.id) {
                    SSHManager.disconnect()
                    _currentConnection.value = null
                }
                loadConnections(); loadCommands()
                showToast("Connection deleted")
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Delete connection failed", "SC-VM-ERR-005", e) }
        }
    }

    fun toggleFavorite(id: Long, fav: Boolean) {
        scope.launch {
            try { db.connectionDao().toggleFavorite(id, fav); loadConnections() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Fav toggle failed", "SC-VM-ERR-006", e) }
        }
    }

    fun connectTo(connection: SSHConnection) {
        _currentConnection.value = connection
        scope.launch {
            db.connectionDao().updateLastConnected(connection.id, System.currentTimeMillis())
            loadConnections()
        }
        SSHManager.connect(connection.host, connection.port, connection.username, connection.password)
        scope.launch {
            // Monitor connection state and stats
            while (SSHManager.connectionState.value == SSHManager.ConnectionState.CONNECTED) {
                _stats.value = SSHManager.stats.value
                _terminalOutput.value = SSHManager.getTerminalOutput()
                delay(500)
            }
        }
    }

    fun disconnect() {
        SSHManager.disconnect()
        _currentConnection.value = null
        showToast("Disconnected")
    }

    fun executeCommand(command: String) {
        val conn = _currentConnection.value ?: return
        SSHManager.executeCommandAsync(conn.host, conn.port, conn.username, conn.password, command)
        scope.launch {
            delay(200)
            _terminalOutput.value = SSHManager.getTerminalOutput()
        }
    }

    fun saveCommand(command: SavedCommand) {
        scope.launch {
            try {
                if (command.id == 0L) db.commandDao().insert(command)
                else db.commandDao().update(command)
                loadCommands()
                showToast("Command saved")
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Save command failed", "SC-VM-ERR-007", e) }
        }
    }

    fun deleteCommand(command: SavedCommand) {
        scope.launch {
            try {
                db.commandDao().delete(command.id)
                loadCommands()
                showToast("Command deleted")
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Delete command failed", "SC-VM-ERR-008", e) }
        }
    }

    fun toggleCommandFavorite(id: Long, fav: Boolean) {
        scope.launch {
            try { db.commandDao().toggleFavorite(id, fav); loadCommands() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Cmd fav toggle failed", "SC-VM-ERR-009", e) }
        }
    }

    fun fetchDockerContainers() {
        val conn = _currentConnection.value ?: return
        scope.launch {
            _dockerContainers.value = SSHManager.fetchDockerContainers(conn.host, conn.port, conn.username, conn.password)
        }
    }

    fun fetchStats() {
        val conn = _currentConnection.value ?: return
        SSHManager.fetchSystemStats(conn.host, conn.port, conn.username, conn.password)
    }

    fun showToast(msg: String) { scope.launch { _toastMessage.emit(msg) } }

    val appVersion: String get() = "1.0.0"

    val githubReleasesUrl: String get() = "https://github.com/jnetaol/SSHCommander/releases"

    val aboutUrl: String get() = "https://jnetaol.com"

    val shareText: String get() = "Manage Linux servers with SSH Commander - One-tap SSH, system stats, Docker controls, SFTP browser, and terminal tabs.\n\nDownload: $githubReleasesUrl"

    override fun onCleared() {
        super.onCleared()
        SSHManager.disconnect()
        scope.cancel()
        DebugLogger.d("AppViewModel", "Cleared", "SC-VM-003")
    }
}
