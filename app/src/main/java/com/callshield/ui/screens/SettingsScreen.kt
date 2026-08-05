package com.callshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
var isLoading by remember { mutableStateOf(false) }

LaunchedEffect(Unit) {  
    viewModel.loadSettings(context)  
}  

Scaffold(  
    topBar = {  
        TopAppBar(  
            title = { Text("ط§ظ„ط¥ط¹ط¯ط§ط¯ط§طھ") },  
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
        // Loading Indicator  
        if (isLoading) {  
            Box(  
                modifier = Modifier.fillMaxWidth(),  
                contentAlignment = Alignment.Center  
            ) {  
                CircularProgressIndicator(  
                    modifier = Modifier.size(48.dp),  
                    color = MaterialTheme.colorScheme.primary  
                )  
            }  
        }  

        // Smart Block Section - ط§ظ„ط­ط¸ط± ط§ظ„ط°ظƒظٹ ظ„ظƒظ„ ط§ظ„ط®ط·ظˆط·  
        SettingsSection(title = "ط§ظ„ط­ط¸ط± ط§ظ„ط°ظƒظٹ") {  
            SettingsSwitchItem(  
                icon = Icons.Default.Security,  
                title = "ط­ط¸ط± ط§ظ„ظ…ظƒط§ظ„ظ…ط§طھ ظ…ظ† ط¬ظ…ظٹط¹ ط§ظ„ط®ط·ظˆط·",  
                subtitle = "ط­ط¸ط± طھظ„ظ‚ط§ط¦ظٹ ظ…ظ† ظƒظ„ ط§ظ„ط®ط·ظˆط· - ظ„ط§ ظٹظ…ظƒظ† ط§ظ„طھظˆط§طµظ„ ط£ط¨ط¯ط§ظ‹",  
                checked = settings?.blockAllSims ?: false,  
                onCheckedChange = {   
                    isLoading = true  
                    viewModel.updateBlockAllSims(context, it)  
                    isLoading = false  
                }  
            )  
            SettingsSwitchItem(  
                icon = Icons.Default.Phone,  
                title = "ط­ط¸ط± ط§ظ„ط£ط±ظ‚ط§ظ… ط§ظ„ظ…ط²ط¹ط¬ط© طھظ„ظ‚ط§ط¦ظٹط§ً",  
                subtitle = "ط­ط¸ط± ط§ظ„ط£ط±ظ‚ط§ظ… ط§ظ„ظ…ط¹ط±ظˆظپط© ظƒظ…ط²ط¹ط¬ط© طھظ„ظ‚ط§ط¦ظٹط§ً",  
                checked = settings?.autoBlockSpam ?: false,  
                onCheckedChange = { viewModel.updateAutoBlockSpam(context, it) }  
            )  
            SettingsSwitchItem(  
                icon = Icons.Default.PhoneDisabled,  
                title = "ط§ظ„ط­ط¸ط± ط§ظ„طھظ„ظ‚ط§ط¦ظٹ ط¨ط¹ط¯ ظ…ط*ط§ظˆظ„ط§طھ ظ…طھظƒط±ط±ط©",  
                subtitle = "ط­ط¸ط± ط§ظ„ط£ط±ظ‚ط§ظ… ط§ظ„طھظٹ طھطھطµظ„ ط£ظƒط«ط± ظ…ظ† ط§ظ„ط*ط¯ ط§ظ„ظ…ط³ظ…ظˆط*",  
                checked = settings?.autoBlockAfterAttempts ?: false,  
                onCheckedChange = { viewModel.updateAutoBlockAfterAttempts(context, it) }  
            )  

            // Accessibility Service  
            SettingsSwitchItem(  
                icon = Icons.Default.Accessibility,  
                title = "تفعيل حظر المكالمات الحقيقي",  
                subtitle = "مطلوب لحظر المكالمات فعلياً (إمكانية الوصول)",  
                checked = isAccessibilityEnabled,  
                onCheckedChange = {   
                    if (it) {  
                        viewModel.enableAccessibilityService(context)  
                    }  
                }  
            )  
              
            if (!isAccessibilityEnabled) {  
                Card(  
                    colors = CardDefaults.cardColors(  
                        containerColor = MaterialTheme.colorScheme.errorContainer  
                    ),  
                    modifier = Modifier  
                        .fillMaxWidth()  
                        .padding(horizontal = 16.dp, vertical = 8.dp)  
                ) {  
                    Column(modifier = Modifier.padding(12.dp)) {  
                        Text(  
                            "لحظر المكالمات فعلياً، لازم تفعل خدمة إمكانية الوصول",  
                            color = MaterialTheme.colorScheme.onErrorContainer  
                        )  
                        Spacer(modifier = Modifier.height(8.dp))  
                        Button(  
                            onClick = { viewModel.openAccessibilitySettings(context) },  
                            modifier = Modifier.fillMaxWidth()  
                        ) {  
                            Text("افتح الإعدادات وفعل الخدمة")  
                        }  
                    }  
                }  
            }

            if (settings?.autoBlockAfterAttempts == true) {  
                OutlinedTextField(  
                    value = (settings?.attemptThreshold ?: 5).toString(),  
                    onValueChange = {  
                        val threshold = it.toIntOrNull() ?: 5  
                        viewModel.updateAttemptThreshold(context, threshold)  
                    },  
                    label = { Text("ط¹ط¯ط¯ ط§ظ„ظ…ط*ط§ظˆظ„ط§طھ ط§ظ„ظ…ط³ظ…ظˆط*ط©") },  
                    modifier = Modifier  
                        .fillMaxWidth()  
                        .padding(horizontal = 16.dp),  
                    singleLine = true  
                )  
            }  
        }  

        // Emergency Bypass Section  
        SettingsSection(title = "ظƒط³ط± ط§ظ„ط*ط¸ط± ط§ظ„ط·ظˆط§ط±ط¦") {  
            SettingsSwitchItem(  
                icon = Icons.Default.Emergency,  
                title = "طھظپط¹ظٹظ„ ظƒط³ط± ط§ظ„ط*ط¸ط± ط§ظ„ط·ظˆط§ط±ط¦",  
                subtitle = "ظپظƒ ط§ظ„ط*ط¸ط± ظ„ظˆ ط§طھطµظ„ ظ†ظپط³ ط§ظ„ط±ظ‚ظ… 3 ظ…ط±ط§طھ ظپظٹ ط¯ظ‚ظٹظ‚طھظٹظ†",  
                checked = settings?.emergencyBypass ?: false,  
                onCheckedChange = { viewModel.updateEmergencyBypass(context, it) }  
            )  
            SettingsSwitchItem(  
                icon = Icons.Default.Message,  
                title = "ظƒط³ط± ط§ظ„ط*ط¸ط± ط¨ظƒظ„ظ…ط© ط§ظ„ط³ط±",  
                subtitle = "ظپظƒ ط§ظ„ط*ط¸ط± ظ„ظˆ ط¨ط¹طھ SMS ظپظٹظ‡ط§ ظƒظ„ظ…ط© ط·ظˆط§ط±ط¦",  
                checked = settings?.keywordBypass ?: false,  
                onCheckedChange = { viewModel.updateKeywordBypass(context, it) }  
            )  
        }  

        // Fake Disconnect Section  
        SettingsSection(title = "ط®ط¯ط¹ط© ط§ظ„ط®ط· ط§ظ„ظ…ظ‚ظپظˆظ„") {  
            SettingsSwitchItem(  
                icon = Icons.Default.PhoneDisabled,  
                title = "طھظپط¹ظٹظ„ ظ†ط؛ظ…ط© ط§ظ„ط®ط· ط§ظ„ظ…ظ‚ظپظˆظ„",  
                subtitle = "طھط´ط؛ظٹظ„ ظ†ط؛ظ…ط© ط§ظ„ط±ظ‚ظ… ط؛ظٹط± ظ…طھط§ط­ ظ„ظ„ظ…ط*ط¸ظˆط±ظٹظ†",  
                checked = settings?.fakeDisconnect ?: false,  
                onCheckedChange = { viewModel.updateFakeDisconnect(context, it) }  
            )  
        }  

        // SMS Filter Section  
        SettingsSection(title = "ظپظ„طھط±ط© ط§ظ„ط±ط³ط§ط¦ظ„") {  
            SettingsSwitchItem(  
                icon = Icons.Default.FilterAlt,  
                title = "ط*ط¸ط± ط§ظ„ط±ط³ط§ط¦ظ„ ط§ظ„ط¥ط¹ظ„ط§ظ†ظٹط©",  
                subtitle = "ط*ط¸ط± SMS ط§ظ„ظ„ظٹ ظپظٹظ‡ط§ ظƒظ„ظ…ط§طھ ظ…ط«ظ„: ط®طµظ…طŒ ط¹ط±ظˆط¶طŒ ظƒظˆط¯",  
                checked = settings?.smsKeywordFilter ?: false,  
                onCheckedChange = { viewModel.updateSmsKeywordFilter(context, it) }  
            )  
        }  

        // Do Not Disturb Section  
        SettingsSection(title = "ظˆط¶ط¹ ط¹ط¯ظ… ط§ظ„ط¥ط²ط¹ط§ط¬") {  
            SettingsSwitchItem(  
                icon = Icons.Default.Notifications,  
                title = "ط*ط¸ط± ط§ظ„ط£ط±ظ‚ط§ظ… ط؛ظٹط± ط§ظ„ظ…ط¹ط±ظˆظپط© ظپظ‚ط·",  
                subtitle = "ط§ظ„ط³ظ…ط§ط* ظ„ط¬ظ‡ط§طھ ط§ظ„ط§طھطµط§ظ„ ظپظ‚ط·",  
                checked = settings?.blockUnknownOnly ?: false,  
                onCheckedChange = { viewModel.updateBlockUnknownOnly(context, it) }  
            )  
        }  

        // Appearance Section  
        SettingsSection(title = "ط§ظ„ظ…ط¸ظ‡ط±") {  
            var darkModeEnabled by remember { mutableStateOf(settings?.darkMode ?: false) }  
            var followSystemEnabled by remember { mutableStateOf(settings?.followSystem ?: true) }  
              
            SettingsSwitchItem(  
                icon = Icons.Default.DarkMode,  
                title = "ط§ظ„ظˆط¶ط¹ ط§ظ„ظ„ظٹظ„ظٹ",  
                subtitle = "طھظپط¹ظٹظ„ ط§ظ„ظˆط¶ط¹ ط§ظ„ظ„ظٹظ„ظٹ",  
                checked = darkModeEnabled && !followSystemEnabled,  
                onCheckedChange = {   
                    darkModeEnabled = it  
                    if (it) followSystemEnabled = false  
                    viewModel.updateDarkMode(context, it)  
                }  
            )  
            SettingsSwitchItem(  
                icon = Icons.Default.PhoneAndroid,  
                title = "ط§طھط¨ط§ط¹ ظ†ط¸ط§ظ… ط§ظ„ط¬ظ‡ط§ط²",  
                subtitle = "طھط؛ظٹظٹط± ط§ظ„ظˆط¶ط¹ ط*ط³ط¨ ط¥ط¹ط¯ط§ط¯ط§طھ ط§ظ„ط¬ظ‡ط§ط²",  
                checked = followSystemEnabled,  
                onCheckedChange = {   
                    followSystemEnabled = it  
                    if (it) darkModeEnabled = false  
                    viewModel.updateFollowSystem(context, it)  
                }  
            )  
        }  

        // Privacy Section  
        SettingsSection(title = "ط§ظ„ط®طµظˆطµظٹط©") {  
            SettingsSwitchItem(  
                icon = Icons.Default.Fingerprint,  
                title = "ظ‚ظپظ„ ط§ظ„طھط·ط¨ظٹظ‚ ط¨ط§ظ„ط¨طµظ…ط©",  
                subtitle = "ط*ظ…ط§ظٹط© ظ‚ط§ط¦ظ…ط© ط§ظ„ظ…ط*ط¸ظˆط±ظٹظ† ط¨ط¨طµظ…ط© ط§ظ„ط¥طµط¨ط¹",  
                checked = settings?.biometricLock ?: false,  
                onCheckedChange = { viewModel.updateBiometricLock(context, it) }  
            )  
        }  

        // Backup Section  
        SettingsSection(title = "ط§ظ„ظ†ط³ط® ط§ظ„ط§ط*طھظٹط§ط·ظٹ") {  
            Button(  
                onClick = {   
                    isLoading = true  
                    viewModel.exportBlockedList(context)  
                    isLoading = false  
                },  
                modifier = Modifier  
                    .fillMaxWidth()  
                    .padding(horizontal = 16.dp, vertical = 4.dp)  
            ) {  
                if (isLoading) {  
                    CircularProgressIndicator(  
                        modifier = Modifier.size(20.dp),  
                        color = MaterialTheme.colorScheme.onPrimary  
                    )  
                } else {  
                    Icon(Icons.Default.Download, contentDescription = null)  
                    Spacer(modifier = Modifier.width(8.dp))  
                    Text("طھطµط¯ظٹط± ط§ظ„ظ‚ط§ط¦ظ…ط©")  
                }  
            }  
            Button(  
                onClick = { viewModel.importBlockedList(context) },  
                modifier = Modifier  
                    .fillMaxWidth()  
                    .padding(horizontal = 16.dp, vertical = 4.dp)  
            ) {  
                Icon(Icons.Default.Upload, contentDescription = null)  
                Spacer(modifier = Modifier.width(8.dp))  
                Text("ط§ط³طھظٹط±ط§ط¯ ط§ظ„ظ‚ط§ط¦ظ…ط©")  
            }  
        }  

        // Danger Zone  
        SettingsSection(title = "ظ…ظ†ط·ظ‚ط© ط§ظ„ط®ط·ط±") {  
            Button(  
                onClick = { viewModel.clearAllData(context) },  
                modifier = Modifier  
                    .fillMaxWidth()  
                    .padding(16.dp),  
                colors = ButtonDefaults.buttonColors(containerColor = BlockRed)  
            ) {  
                Icon(Icons.Default.DeleteForever, contentDescription = null)  
                Spacer(modifier = Modifier.width(8.dp))  
                Text("ظ…ط³ط­ ط¬ظ…ظٹط¹ ط§ظ„ط¨ظٹط§ظ†ط§طھ")  
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
