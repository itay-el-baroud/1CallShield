package com.callshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callshield.ui.theme.BlockRed
import com.callshield.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Smart Block Section
            SettingsSection(title = "Smart Block") {
                SettingsSwitchItem(
                    icon = Icons.Default.Security,
                    title = "Auto-block spam numbers",
                    subtitle = "Block known spam numbers automatically",
                    checked = settings?.autoBlockSpam ?: false,
                    onCheckedChange = { viewModel.updateAutoBlockSpam(context, it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Phone,
                    title = "Auto-block after X attempts",
                    subtitle = "Block numbers that call more than threshold",
                    checked = settings?.autoBlockAfterAttempts ?: false,
                    onCheckedChange = { viewModel.updateAutoBlockAfterAttempts(context, it) }
                )
                if (settings?.autoBlockAfterAttempts == true) {
                    OutlinedTextField(
                        value = (settings?.attemptThreshold ?: 5).toString(),
                        onValueChange = {
                            val threshold = it.toIntOrNull() ?: 5
                            viewModel.updateAttemptThreshold(context, threshold)
                        },
                        label = { Text("Attempt threshold") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true
                    )
                }
            }

            // Do Not Disturb Section
            SettingsSection(title = "Do Not Disturb") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Block unknown numbers only",
                    subtitle = "Allow only contacts",
                    checked = settings?.blockUnknownOnly ?: false,
                    onCheckedChange = { viewModel.updateBlockUnknownOnly(context, it) }
                )
            }

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Enable dark theme",
                    checked = settings?.darkMode ?: false,
                    onCheckedChange = { viewModel.updateDarkMode(context, it) }
                )
            }

            // Danger Zone
            SettingsSection(title = "Danger Zone") {
                Button(
                    onClick = { viewModel.clearAllData(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlockRed)
                ) {
                    Text("Clear All Data")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        content()
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
