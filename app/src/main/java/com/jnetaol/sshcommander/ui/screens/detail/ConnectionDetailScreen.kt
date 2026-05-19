package com.jnetaol.sshcommander.ui.screens.detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.sshcommander.data.model.SSHConnection
import com.jnetaol.sshcommander.data.model.SavedCommand
import com.jnetaol.sshcommander.engine.SSHManager
import com.jnetaol.sshcommander.ui.components.*
import com.jnetaol.sshcommander.ui.screens.AppViewModel
import com.jnetaol.sshcommander.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConnectionDetailScreen(
    connectionId: Long,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenTerminal: () -> Unit
) {
    val connections by viewModel.connections.collectAsState()
    val connection = connections.find { it.id == connectionId }
    val connectionState by viewModel.connectionState.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val dockerContainers by viewModel.dockerContainers.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(connectionId) {
        viewModel.loadConnections()
    }

    Column(Modifier.fillMaxSize().background(SCBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = SCTextPrimary) }
            Text(connection?.name ?: "Connection", color = SCTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton({ showEditDialog = true }) { Icon(Icons.Default.Edit, null, tint = SCTextSecondary) }
        }

        if (connection == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Connection not found", color = SCTextMuted)
            }
            return
        }

        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Server Details", color = SCPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        DetailRow("Host", connection.host)
                        DetailRow("Port", connection.port.toString())
                        DetailRow("Username", connection.username)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Password", color = SCTextMuted, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (showPassword) connection.password else "••••••••", color = SCTextPrimary, fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                TextButton({ showPassword = !showPassword }) {
                                    Text(if (showPassword) "Hide" else "Show", color = SCTextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                        if (connection.lastConnected > 0) {
                            DetailRow("Last Connected", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(connection.lastConnected)))
                        }
                        DetailRow("Created", SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(connection.createdAt)))
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isConnected = connectionState == SSHManager.ConnectionState.CONNECTED
                    val isConnecting = connectionState == SSHManager.ConnectionState.CONNECTING

                    GlowButton(
                        if (isConnected) "Disconnect" else "Connect",
                        if (isConnected) Icons.Default.Close else Icons.Default.PlayArrow,
                        glowColor = if (isConnected) SCError else SCSuccess,
                        enabled = !isConnecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isConnected) viewModel.disconnect()
                        else viewModel.connectTo(connection)
                    }

                    GlowButton(
                        "Terminal",
                        Icons.Default.Terminal,
                        glowColor = SCNeonCyan,
                        modifier = Modifier.weight(1f),
                        enabled = isConnected
                    ) { onOpenTerminal() }
                }
            }

            item {
                NeonCard(borderColor = SCSecondary.copy(alpha = 0.3f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Live System Stats", color = SCSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        if (connectionState == SSHManager.ConnectionState.CONNECTED) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard("CPU", stats.cpuUsage, Icons.Default.Memory, SCNeonGreen, Modifier.weight(1f))
                                StatCard("Memory", stats.memoryUsage, Icons.Default.Storage, SCNeonCyan, Modifier.weight(1f))
                                StatCard("Disk", stats.diskUsage, Icons.Default.DiscFull, SCWarning, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard("Load", stats.loadAverage, Icons.Default.Speed, SCNeonOrange, Modifier.weight(1f))
                                StatCard("Uptime", stats.uptime, Icons.Default.Timer, SCNeonPurple, Modifier.weight(1f))
                                StatCard("Procs", stats.processes, Icons.Default.Apps, SCInfo, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("OS: ${stats.osInfo}", color = SCTextSecondary, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            GlowButton("Refresh Stats", Icons.Default.Refresh, glowColor = SCSecondary) {
                                viewModel.fetchStats()
                            }
                        } else {
                            Text("Connect to view live system stats", color = SCTextMuted, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                NeonCard(borderColor = SCPrimary.copy(alpha = 0.3f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Quick Actions", color = SCPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        val quickCommands = listOf(
                            Triple("Uptime", "uptime", Icons.Default.Timer),
                            Triple("Disk Usage", "df -h", Icons.Default.DiscFull),
                            Triple("Memory", "free -m", Icons.Default.Storage),
                            Triple("Processes", "ps aux --sort=-%mem | head -10", Icons.Default.Apps),
                            Triple("Docker PS", "docker ps", Icons.Default.Widgets),
                            Triple("Network", "ip addr show", Icons.Default.SettingsEthernet),
                            Triple("Updates", "apt list --upgradable 2>/dev/null | tail -n +2", Icons.Default.SystemUpdateAlt),
                            Triple("Reboot", "sudo reboot", Icons.Default.PowerSettingsNew)
                        )
                        quickCommands.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (label, cmd, icon) ->
                                    GlowButton(
                                        label, icon, glowColor = SCPrimary,
                                        modifier = Modifier.weight(1f),
                                        enabled = connectionState == SSHManager.ConnectionState.CONNECTED
                                    ) { viewModel.executeCommand(cmd) }
                                }
                                if (row.size < 2) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            item {
                NeonCard(borderColor = SCNeonCyan.copy(alpha = 0.3f)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Docker Containers", color = SCNeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            GlowButton("Refresh", Icons.Default.Refresh, glowColor = SCNeonCyan) { viewModel.fetchDockerContainers() }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (dockerContainers.isEmpty() && connectionState == SSHManager.ConnectionState.CONNECTED) {
                            Text("No containers or Docker not installed", color = SCTextMuted, fontSize = 13.sp)
                        } else if (connectionState != SSHManager.ConnectionState.CONNECTED) {
                            Text("Connect to view Docker containers", color = SCTextMuted, fontSize = 13.sp)
                        } else {
                            dockerContainers.forEach { container ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(if (container.running) SCSuccess else SCError, RoundedCornerShape(4.dp)))
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(container.name, color = SCTextPrimary, fontSize = 13.sp)
                                        Text(container.status, color = SCTextMuted, fontSize = 11.sp)
                                    }
                                    StatusBadge(if (container.running) "UP" else "DOWN", if (container.running) SCSuccess else SCError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && connection != null) {
        EditConnectionDialog(
            connection = connection,
            onDismiss = { showEditDialog = false },
            onSave = { viewModel.saveConnection(it); showEditDialog = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SCTextMuted, fontSize = 14.sp)
        Text(value, color = SCTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditConnectionDialog(
    connection: SSHConnection,
    onDismiss: () -> Unit,
    onSave: (SSHConnection) -> Unit
) {
    var name by remember { mutableStateOf(connection.name) }
    var host by remember { mutableStateOf(connection.host) }
    var port by remember { mutableStateOf(connection.port.toString()) }
    var username by remember { mutableStateOf(connection.username) }
    var password by remember { mutableStateOf(connection.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Connection", color = SCPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonTextField(name, { name = it }, "Server Name", leadingIcon = Icons.Default.Label)
                NeonTextField(host, { host = it }, "Host / IP", leadingIcon = Icons.Default.Dns)
                NeonTextField(port, { port = it }, "Port", leadingIcon = Icons.Default.SettingsEthernet)
                NeonTextField(username, { username = it }, "Username", leadingIcon = Icons.Default.Person)
                NeonTextField(password, { password = it }, "Password", leadingIcon = Icons.Default.Lock)
            }
        },
        confirmButton = {
            GlowButton("Save", Icons.Default.Save, glowColor = SCPrimary, enabled = name.isNotBlank() && host.isNotBlank()) {
                onSave(connection.copy(name = name, host = host, port = port.toIntOrNull() ?: 22, username = username, password = password))
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = SCTextMuted) } },
        containerColor = SCSurface
    )
}
