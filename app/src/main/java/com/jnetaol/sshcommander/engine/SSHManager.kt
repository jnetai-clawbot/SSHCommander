package com.jnetaol.sshcommander.engine

import com.jnetaol.sshcommander.logger.DebugLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader

data class SSHResult(val output: String, val error: String = "", val exitCode: Int = 0)

data class SystemStats(
    val hostname: String = "unknown",
    val cpuUsage: String = "0%",
    val memoryUsage: String = "0%",
    val diskUsage: String = "0%",
    val uptime: String = "0m",
    val loadAverage: String = "0.00",
    val processes: String = "0",
    val osInfo: String = "Linux"
)

data class DockerContainer(
    val id: String, val name: String, val image: String,
    val status: String, val ports: String, val running: Boolean
)

object SSHManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeSession: Any? = null
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()
    private val _stats = MutableStateFlow(SystemStats())
    val stats: StateFlow<SystemStats> = _stats.asStateFlow()

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    fun connect(host: String, port: Int, username: String, password: String) {
        scope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            DebugLogger.i("SSHManager", "Connecting to $host:$port as $username", "SC-SSH-001")
            try {
                val runtime = Runtime.getRuntime()
                val process = runtime.exec(arrayOf("sshpass", "-p", password, "ssh",
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "UserKnownHostsFile=/dev/null",
                    "-p", port.toString(),
                    "$username@$host", "echo SSH_CONNECTED"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val result = reader.readLine()
                process.waitFor()
                if (result == "SSH_CONNECTED") {
                    _connectionState.value = ConnectionState.CONNECTED
                    DebugLogger.i("SSHManager", "Connected to $host:$port", "SC-SSH-002")
                    fetchSystemStats(host, port, username, password)
                } else {
                    _connectionState.value = ConnectionState.ERROR
                    DebugLogger.e("SSHManager", "Connection handshake failed", "SC-SSH-ERR-001")
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                DebugLogger.e("SSHManager", "Connection failed: ${e.message}", "SC-SSH-ERR-002", e)
            }
        }
    }

    fun executeCommand(host: String, port: Int, username: String, password: String, command: String): SSHResult {
        DebugLogger.d("SSHManager", "Executing: $command", "SC-SSH-003")
        return try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("sshpass", "-p", password, "ssh",
                "-o", "StrictHostKeyChecking=no", "-o", "UserKnownHostsFile=/dev/null",
                "-p", port.toString(), "$username@$host", command))
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()
            SSHResult(output.trim(), error.trim(), exitCode)
        } catch (e: Exception) {
            DebugLogger.e("SSHManager", "Command failed: ${e.message}", "SC-SSH-ERR-003", e)
            SSHResult("", e.message ?: "Unknown error", -1)
        }
    }

    fun executeCommandAsync(host: String, port: Int, username: String, password: String, command: String) {
        scope.launch {
            val result = executeCommand(host, port, username, password, command)
            _terminalOutput.value = result.output
            if (result.error.isNotEmpty()) {
                _terminalOutput.value += "\n[STDERR] ${result.error}"
            }
        }
    }

    fun fetchSystemStats(host: String, port: Int, username: String, password: String) {
        scope.launch {
            try {
                val statsScript = """
                    echo "HOSTNAME:$(hostname)"
                    CPU=${'$'}(top -bn1 | grep "Cpu(s)" | awk '{print ${'$'}2}' | cut -d'%' -f1)
                    echo "CPU:${'$'}{CPU}%"
                    MEM=${'$'}(free -m | awk 'NR==2{printf "%.1f%%", ${'$'}3*100/${'$'}2}')
                    echo "MEM:${'$'}{MEM}"
                    DISK=${'$'}(df -h / | awk 'NR==2{print ${'$'}5}')
                    echo "DISK:${'$'}{DISK}"
                    UP=${'$'}(uptime -p 2>/dev/null || uptime | awk -F'up ' '{print ${'$'}2}' | awk -F',' '{print ${'$'}1}')
                    echo "UPTIME:${'$'}{UP}"
                    LA=${'$'}(uptime | awk -F'load average:' '{print ${'$'}2}' | tr -d ' ')
                    echo "LOAD:${'$'}{LA}"
                    PROCS=${'$'}(ps aux --no-headers | wc -l)
                    echo "PROCS:${'$'}{PROCS}"
                    OS=${'$'}(cat /etc/os-release 2>/dev/null | grep PRETTY_NAME | cut -d'"' -f2 || echo "Linux")
                    echo "OS:${'$'}{OS}"
                """.trimIndent().replace("\n", "; ")
                val result = executeCommand(host, port, username, password, statsScript)
                val lines = result.output.split(";").map { it.trim() }
                val statMap = mutableMapOf<String, String>()
                lines.forEach { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) statMap[parts[0]] = parts[1].trim()
                }
                _stats.value = SystemStats(
                    hostname = statMap["HOSTNAME"] ?: "unknown",
                    cpuUsage = statMap["CPU"] ?: "0%",
                    memoryUsage = statMap["MEM"] ?: "0%",
                    diskUsage = statMap["DISK"] ?: "0%",
                    uptime = statMap["UPTIME"] ?: "unknown",
                    loadAverage = statMap["LOAD"] ?: "0.00",
                    processes = statMap["PROCS"] ?: "0",
                    osInfo = statMap["OS"] ?: "Linux"
                )
                DebugLogger.d("SSHManager", "Stats fetched: CPU=${_stats.value.cpuUsage}", "SC-SSH-004")
            } catch (e: Exception) {
                DebugLogger.e("SSHManager", "Stats fetch failed", "SC-SSH-ERR-004", e)
            }
        }
    }

    fun fetchDockerContainers(host: String, port: Int, username: String, password: String): List<DockerContainer> {
        DebugLogger.d("SSHManager", "Fetching Docker containers", "SC-SSH-005")
        return try {
            val result = executeCommand(host, port, username, password,
                "docker ps -a --format '{{.ID}}|{{.Names}}|{{.Image}}|{{.Status}}|{{.Ports}}' 2>/dev/null || echo ''")
            if (result.output.isBlank()) return emptyList()
            result.output.lines().filter { it.isNotBlank() }.map { line ->
                val parts = line.split("|", limit = 5)
                if (parts.size >= 4) {
                    DockerContainer(
                        id = parts.getOrElse(0) { "" },
                        name = parts.getOrElse(1) { "" },
                        image = parts.getOrElse(2) { "" },
                        status = parts.getOrElse(3) { "" },
                        ports = parts.getOrElse(4) { "" },
                        running = parts[3].contains("Up", ignoreCase = true)
                    )
                } else DockerContainer("", "", "", "", "", false)
            }
        } catch (e: Exception) {
            DebugLogger.e("SSHManager", "Docker fetch failed", "SC-SSH-ERR-005", e)
            emptyList()
        }
    }

    fun listFiles(host: String, port: Int, username: String, password: String, path: String = "/"): List<String> {
        DebugLogger.d("SSHManager", "Listing files at $path", "SC-SSH-006")
        return try {
            val result = executeCommand(host, port, username, password,
                "ls -la '$path' 2>/dev/null | tail -n +2")
            result.output.lines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            DebugLogger.e("SSHManager", "File listing failed", "SC-SSH-ERR-006", e)
            emptyList()
        }
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        activeSession = null
        DebugLogger.i("SSHManager", "Disconnected", "SC-SSH-007")
    }

    fun getTerminalOutput(): String = _terminalOutput.value

    fun clearTerminalOutput() {
        _terminalOutput.value = ""
    }
}
