package com.jnetaol.sshcommander.ui.screens.terminal

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.sshcommander.data.model.SavedCommand
import com.jnetaol.sshcommander.engine.SSHManager
import com.jnetaol.sshcommander.ui.components.*
import com.jnetaol.sshcommander.ui.screens.AppViewModel
import com.jnetaol.sshcommander.ui.theme.*

@Composable
fun TerminalScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val connection by viewModel.currentConnection.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    val commands by viewModel.commands.collectAsState()

    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Terminal", "Commands", "Stats")

    val connCommands = commands.filter { it.connectionId == 0L || it.connectionId == connection?.id }

    LaunchedEffect(terminalOutput) {
        if (terminalOutput.isNotBlank()) listState.animateScrollToItem(Int.MAX_VALUE)
    }

    Column(Modifier.fillMaxSize().background(SCBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = SCTextPrimary) }
            Column(Modifier.weight(1f)) {
                Text(connection?.name ?: "Terminal", color = SCTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (connection != null) {
                    Text("${connection?.username}@${connection?.host}", color = SCTextMuted, fontSize = 12.sp)
                }
            }
            when (connectionState) {
                SSHManager.ConnectionState.CONNECTED -> StatusBadge("CONNECTED", SCSuccess)
                SSHManager.ConnectionState.CONNECTING -> StatusBadge("CONNECTING", SCWarning)
                SSHManager.ConnectionState.ERROR -> StatusBadge("ERROR", SCError)
                SSHManager.ConnectionState.DISCONNECTED -> StatusBadge("DISCONNECTED", SCTextMuted)
            }
        }

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = SCSurface,
            contentColor = SCPrimary,
            divider = { Divider(color = SCSurfaceVariant) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontSize = 13.sp) },
                    selectedContentColor = SCPrimary,
                    unselectedContentColor = SCTextMuted
                )
            }
        }

        when (activeTab) {
            0 -> TerminalView(commandInput, { commandInput = it }, viewModel, terminalOutput, listState)
            1 -> CommandsView(viewModel, connCommands, commandInput, { commandInput = it })
            2 -> StatsView(viewModel)
        }
    }
}

@Composable
fun TerminalView(
    commandInput: String,
    onInputChange: (String) -> Unit,
    viewModel: AppViewModel,
    terminalOutput: String,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth().background(SCBackground).padding(8.dp)) {
            if (terminalOutput.isBlank()) {
                Text("Ready. Type a command to execute.", color = SCTextMuted, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(terminalOutput.lines()) { line ->
                        Text(line, color = SCPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 1.dp))
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$ ", color = SCNeonCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Text(commandInput, color = SCTextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Surface(Modifier.fillMaxWidth(), color = SCSurface) {
            Row(Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = commandInput, onValueChange = onInputChange,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(SCSlightlyDarker).padding(12.dp),
                    textStyle = TextStyle(color = SCTextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(SCPrimary),
                    decorationBox = { inner ->
                        Box {
                            if (commandInput.isEmpty()) Text("Enter command...", color = SCTextMuted, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                            inner()
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            viewModel.executeCommand(commandInput)
                            onInputChange("")
                        }
                    },
                    Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Send, "Execute", tint = SCPrimary)
                }
            }
        }
    }
}

@Composable
fun CommandsView(
    viewModel: AppViewModel,
    commands: List<SavedCommand>,
    input: String,
    onInputChange: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val connection = viewModel.currentConnection.collectAsState().value

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Saved Commands", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            GlowButton("Add", Icons.Default.Add, onClick = { showAddDialog = true }, glowColor = SCPrimary)
        }

        if (commands.isEmpty()) {
            EmptyState(Icons.Default.Code, "No Saved Commands", "Add commands for quick execution")
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(commands, key = { it.id }) { cmd ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.executeCommand(cmd.command)
                            viewModel.saveCommand(cmd.copy(lastUsed = System.currentTimeMillis()))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SCCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SCSurfaceVariant)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, null, tint = if (cmd.isFavorite) SCWarning else SCNeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(cmd.name, color = SCTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(cmd.command, color = SCPrimary.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                            }
                            IconButton({ viewModel.toggleCommandFavorite(cmd.id, !cmd.isFavorite) }, Modifier.size(32.dp)) {
                                Icon(if (cmd.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null,
                                    tint = if (cmd.isFavorite) SCWarning else SCTextMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton({ viewModel.deleteCommand(cmd) }, Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = SCTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCommandDialog(
            connectionId = connection?.id ?: 0,
            onDismiss = { showAddDialog = false },
            onSave = { viewModel.saveCommand(it); showAddDialog = false }
        )
    }
}

@Composable
fun StatsView(viewModel: AppViewModel) {
    val stats by viewModel.stats.collectAsState()
    val dockerContainers by viewModel.dockerContainers.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("System Stats", color = SCTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                GlowButton("Refresh", Icons.Default.Refresh, onClick = { viewModel.fetchStats() }, glowColor = SCSecondary)
            }
        }

        if (connectionState == SSHManager.ConnectionState.CONNECTED) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SCCard)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(stats.osInfo, color = SCSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Host: ${stats.hostname}", color = SCTextMuted, fontSize = 12.sp)
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("CPU", stats.cpuUsage, Icons.Default.Memory, SCNeonGreen, Modifier.weight(1f))
                    StatCard("Memory", stats.memoryUsage, Icons.Default.Storage, SCNeonCyan, Modifier.weight(1f))
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Disk", stats.diskUsage, Icons.Default.DiscFull, SCWarning, Modifier.weight(1f))
                    StatCard("Load", stats.loadAverage, Icons.Default.Speed, SCNeonOrange, Modifier.weight(1f))
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Uptime", stats.uptime, Icons.Default.Timer, SCNeonPurple, Modifier.weight(1f))
                    StatCard("Processes", stats.processes, Icons.Default.Apps, SCInfo, Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Docker Containers", color = SCTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    GlowButton("Refresh", Icons.Default.Refresh, onClick = { viewModel.fetchDockerContainers() }, glowColor = SCSecondary)
                }
            }

            if (dockerContainers.isEmpty()) {
                item { Text("No containers found or Docker not installed", color = SCTextMuted, fontSize = 13.sp, modifier = Modifier.padding(8.dp)) }
            } else {
                items(dockerContainers) { container ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SCCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SCSurfaceVariant)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if (container.running) SCSuccess else SCError))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(container.name, color = SCTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${container.image} — ${container.status}", color = SCTextMuted, fontSize = 11.sp, maxLines = 1)
                            }
                            StatusBadge(if (container.running) "RUNNING" else "STOPPED", if (container.running) SCSuccess else SCError)
                        }
                    }
                }
            }
        } else {
            item { Text("Connect to a server to view system stats", color = SCTextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 32.dp)) }
        }
    }
}

@Composable
fun AddCommandDialog(
    connectionId: Long,
    onDismiss: () -> Unit,
    onSave: (SavedCommand) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Command", color = SCPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonTextField(name, { name = it }, "Command Name", leadingIcon = Icons.Default.Label)
                NeonTextField(command, { command = it }, "Command", leadingIcon = Icons.Default.Terminal, glowColor = SCNeonCyan)
                NeonTextField(description, { description = it }, "Description (optional)", leadingIcon = Icons.Default.Info)
            }
        },
        confirmButton = {
            GlowButton("Save", Icons.Default.Save, onClick = {
                onSave(SavedCommand(connectionId = connectionId, name = name, command = command, description = description))
            }, glowColor = SCPrimary, enabled = name.isNotBlank() && command.isNotBlank())
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = SCTextMuted) } },
        containerColor = SCSurface
    )
}

val SCSlightlyDarker = androidx.compose.ui.graphics.Color(0xFF0D0D1A)
