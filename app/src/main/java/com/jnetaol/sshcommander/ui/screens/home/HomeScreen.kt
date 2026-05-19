package com.jnetaol.sshcommander.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.sshcommander.data.model.SSHConnection
import com.jnetaol.sshcommander.data.model.SavedCommand
import com.jnetaol.sshcommander.ui.components.*
import com.jnetaol.sshcommander.ui.screens.AppViewModel
import com.jnetaol.sshcommander.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToConnection: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onQuickConnect: (SSHConnection) -> Unit
) {
    val connections by viewModel.connections.collectAsState()
    val commands by viewModel.commands.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SCBackground)) {
        Row(Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("SSH Commander", color = SCTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Linux Server Manager", color = SCTextMuted, fontSize = 13.sp)
            }
            Row {
                IconButton({ showAddDialog = true }) {
                    Icon(Icons.Default.Add, null, tint = SCPrimary, modifier = Modifier.size(28.dp))
                }
                IconButton(onNavigateToSettings) {
                    Icon(Icons.Default.Settings, null, tint = SCTextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchConnections(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search connections...", color = SCTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SCTextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) IconButton({ searchQuery = ""; viewModel.searchConnections("") }) {
                    Icon(Icons.Default.Clear, null, tint = SCTextMuted)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SCTextPrimary, unfocusedTextColor = SCTextPrimary,
                focusedBorderColor = SCPrimary.copy(alpha = 0.5f), unfocusedBorderColor = SCSurfaceVariant,
                cursorColor = SCPrimary
            ),
            shape = RoundedCornerShape(12.dp), singleLine = true
        )

        if (connections.isEmpty()) {
            EmptyState(Icons.Default.Dns, "No Connections", "Tap + to add your first SSH server")
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(connections, key = { it.id }) { conn ->
                    ConnectionCard(
                        connection = conn,
                        onClick = { onNavigateToConnection(conn.id) },
                        onQuickConnect = { onQuickConnect(conn) },
                        onToggleFavorite = { viewModel.toggleFavorite(conn.id, !conn.isFavorite) },
                        onDelete = { viewModel.deleteConnection(conn) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddConnectionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { conn ->
                viewModel.saveConnection(conn)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ConnectionCard(
    connection: SSHConnection,
    onClick: () -> Unit,
    onQuickConnect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val connColor = SCConnectionColors[connection.colorIndex % SCConnectionColors.size]
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SCCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SCSurfaceVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Brush.radialGradient(listOf(connColor.copy(alpha = 0.3f), connColor.copy(alpha = 0.05f)))),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Dns, null, tint = connColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(connection.name, color = SCTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (connection.isFavorite) {
                        Icon(Icons.Default.Star, null, tint = SCWarning, modifier = Modifier.size(14.dp).padding(start = 4.dp))
                    }
                }
                Text("${connection.username}@${connection.host}:${connection.port}", color = SCTextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (connection.lastConnected > 0) {
                    Text("Last: ${SimpleDateFormat("MMM dd HH:mm", Locale.US).format(Date(connection.lastConnected))}", color = SCTextMuted.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
            IconButton(onQuickConnect, Modifier.size(36.dp)) {
                Icon(Icons.Default.PlayArrow, "Connect", tint = SCNeonGreen, modifier = Modifier.size(20.dp))
            }
            IconButton(onToggleFavorite, Modifier.size(36.dp)) {
                Icon(if (connection.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null,
                    tint = if (connection.isFavorite) SCWarning else SCTextMuted, modifier = Modifier.size(18.dp))
            }
            IconButton({ showDeleteConfirm = true }, Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = SCTextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Connection", color = SCTextPrimary) },
            text = { Text("Delete \"${connection.name}\"?", color = SCTextSecondary) },
            confirmButton = { TextButton({ onDelete(); showDeleteConfirm = false }) { Text("Delete", color = SCError) } },
            dismissButton = { TextButton({ showDeleteConfirm = false }) { Text("Cancel", color = SCTextMuted) } },
            containerColor = SCSurface, titleContentColor = SCTextPrimary, textContentColor = SCTextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConnectionDialog(onDismiss: () -> Unit, onSave: (SSHConnection) -> Unit) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Connection", color = SCPrimary, fontWeight = FontWeight.Bold) },
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
            GlowButton("Save", Icons.Default.Save, onClick = {
                onSave(SSHConnection(
                    name = name.ifBlank { "$username@$host" },
                    host = host, port = port.toIntOrNull() ?: 22,
                    username = username.ifBlank { "root" }, password = password
                ))
            }, glowColor = SCPrimary, enabled = name.isNotBlank() && host.isNotBlank())
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = SCTextMuted) } },
        containerColor = SCSurface
    )
}
