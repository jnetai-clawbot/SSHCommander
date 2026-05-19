package com.jnetaol.sshcommander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jnetaol.sshcommander.ui.screens.AppViewModel
import com.jnetaol.sshcommander.ui.screens.detail.ConnectionDetailScreen
import com.jnetaol.sshcommander.ui.screens.home.HomeScreen
import com.jnetaol.sshcommander.ui.screens.settings.SettingsScreen
import com.jnetaol.sshcommander.ui.screens.terminal.TerminalScreen
import com.jnetaol.sshcommander.ui.theme.SSHCommanderTheme
import com.jnetaol.sshcommander.logger.DebugLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DebugLogger.d("MainActivity", "onCreate", "SC-MA-001")
        setContent {
            SSHCommanderTheme {
                val viewModel: AppViewModel = viewModel()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var selectedConnectionId by remember { mutableStateOf(0L) }

                Surface(Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToConnection = { id ->
                                selectedConnectionId = id
                                currentScreen = Screen.ConnectionDetail
                            },
                            onNavigateToSettings = { currentScreen = Screen.Settings },
                            onQuickConnect = { conn ->
                                selectedConnectionId = conn.id
                                viewModel.connectTo(conn)
                                currentScreen = Screen.Terminal
                            }
                        )
                        Screen.ConnectionDetail -> ConnectionDetailScreen(
                            connectionId = selectedConnectionId,
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home },
                            onOpenTerminal = { currentScreen = Screen.Terminal }
                        )
                        Screen.Terminal -> TerminalScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.ConnectionDetail }
                        )
                        Screen.Settings -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        DebugLogger.d("MainActivity", "onDestroy", "SC-MA-002")
        super.onDestroy()
    }

    private enum class Screen { Home, ConnectionDetail, Terminal, Settings }
}
