package com.jnetaol.sshcommander.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.sshcommander.ui.components.*
import com.jnetaol.sshcommander.ui.screens.AppViewModel
import com.jnetaol.sshcommander.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(Modifier.fillMaxSize().background(SCBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = SCTextPrimary) }
            Text("Settings", color = SCTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NeonCard {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Terminal, null, Modifier.size(48.dp), tint = SCPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("SSH Commander", color = SCTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Linux Server Manager", color = SCTextMuted, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Version ${viewModel.appVersion}", color = SCPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("About", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "SSH Commander is a polished SSH dashboard for managing Linux servers. Features include one-tap SSH connect, saved commands, live system stats, Docker controls, SFTP browser, and terminal tabs.",
                            color = SCTextSecondary, fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("Made By ", color = SCTextSecondary, fontSize = 14.sp)
                            Text("jnetai.com", color = SCPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, Modifier.size(14.dp), tint = SCSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                viewModel.aboutUrl,
                                color = SCSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(viewModel.aboutUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Updates", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Current Version", color = SCTextMuted, fontSize = 12.sp)
                                Text("v${viewModel.appVersion}", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                            StatusBadge("Up to date", SCSuccess)
                        }
                        Spacer(Modifier.height(12.dp))
                        GlowButton(
                            "Check For Updates",
                            Icons.Default.SystemUpdateAlt,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(viewModel.githubReleasesUrl))
                                context.startActivity(intent)
                            },
                            glowColor = SCSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Share", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlowButton(
                                "Share App",
                                Icons.Default.Share,
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "SSH Commander - Linux Server Manager")
                                        putExtra(Intent.EXTRA_TEXT, viewModel.shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share SSH Commander"))
                                },
                                glowColor = SCPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            GlowButton(
                                "Copy Link",
                                Icons.Default.ContentCopy,
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(viewModel.githubReleasesUrl))
                                    viewModel.showToast("Link copied!")
                                },
                                glowColor = SCSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Features", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FeatureRow("One-tap SSH connect to Linux servers")
                        FeatureRow("Saved commands for quick execution")
                        FeatureRow("Live system stats monitoring")
                        FeatureRow("Docker container controls")
                        FeatureRow("Interactive terminal with tabs")
                        FeatureRow("SFTP file browser")
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Legal", color = SCTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("MIT License", color = SCTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Copyright (c) 2024 jnetai.com", color = SCTextMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This software is provided \"as-is\" without warranty. Manage your servers responsibly.",
                            color = SCTextMuted, fontSize = 12.sp
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = SCSuccess)
        Spacer(Modifier.width(8.dp))
        Text(text, color = SCTextSecondary, fontSize = 13.sp)
    }
}
