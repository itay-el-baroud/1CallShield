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
                title = { Text("الإعدادات") },
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

            // Smart Block Section - الحظر الذكي لكل الخطوط
            SettingsSection(title = "الحظر الذكي") {
                SettingsSwitchItem(
                    icon = Icons.Default.Security,
                    title = "حظر المكالمات من جميع الخطوط",
                    subtitle = "حظر تلقائي من كل الخطوط - لا يمكن التواصل أبداً",
                    checked = settings?.blockAllSims ?: false,
                    onCheckedChange = { 
                        isLoading = true
                        viewModel.updateBlockAllSims(context, it)
                        isLoading = false
                    }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Phone,
                    title = "حظر الأرقام المزعجة تلقائياً",
                    subtitle = "حظر الأرقام المعروفة كمزعجة تلقائياً",
                    checked = settings?.autoBlockSpam ?: false,
                    onCheckedChange = { viewModel.updateAutoBlockSpam(context, it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.PhoneDisabled,
                    title = "الحظر التلقائي بعد محاولات متكررة",
                    subtitle = "حظر الأرقام التي تتصل أكثر من الحد المسموح",
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
                        label = { Text("عدد المحاولات المسموحة") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true
                    )
                }
            }

            // Accessibility Service Section
            SettingsSection(title = "خدمة إمكانية الوصول") {
                SettingsSwitchItem(
                    icon = Icons.Default.SettingsAccessibility,
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
            }

            // Emergency Bypass Section
            SettingsSection(title = "كسر الحظر الطوارئ") {
                SettingsSwitchItem(
                    icon = Icons.Default.Emergency,
                    title = "تفعيل كسر الحظر الطوارئ",
                    subtitle = "فك الحظر لو اتصل نفس الرقم 3 مرات في دقيقتين",
                    checked = settings?.emergencyBypass ?: false,
                    onCheckedChange = { viewModel.updateEmergencyBypass(context, it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Message,
                    title = "كسر الحظر بكلمة السر",
                    subtitle = "فك الحظر لو بعت SMS فيها كلمة طوارئ",
                    checked = settings?.keywordBypass ?: false,
                    onCheckedChange = { viewModel.updateKeywordBypass(context, it) }
                )
            }

            // Fake Disconnect Section
            SettingsSection(title = "خدعة الخط المقفول") {
                SettingsSwitchItem(
                    icon = Icons.Default.PhoneDisabled,
                    title = "تفعيل نغمة الخط المقفول",
                    subtitle = "تشغيل نغمة الرقم غير متاح للمحظورين",
                    checked = settings?.fakeDisconnect ?: false,
                    onCheckedChange = { viewModel.updateFakeDisconnect(context, it) }
                )
            }

            // SMS Filter Section
            SettingsSection(title = "فلترة الرسائل") {
                SettingsSwitchItem(
                    icon = Icons.Default.FilterAlt,
                    title = "حظر الرسائل الإعلانية",
                    subtitle = "حظر SMS اللي فيها كلمات مثل: خصم، عروض، كود",
                    checked = settings?.smsKeywordFilter ?: false,
                    onCheckedChange = { viewModel.updateSmsKeywordFilter(context, it) }
                )
            }

            // Do Not Disturb Section
            SettingsSection(title = "وضع عدم الإزعاج") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "حظر الأرقام غير المعروفة فقط",
                    subtitle = "السماح لجهات الاتصال فقط",
                    checked = settings?.blockUnknownOnly ?: false,
                    onCheckedChange = { viewModel.updateBlockUnknownOnly(context, it) }
                )
            }

            // Appearance Section
            SettingsSection(title = "المظهر") {
                var darkModeEnabled by remember { mutableStateOf(settings?.darkMode ?: false) }
                var followSystemEnabled by remember { mutableStateOf(settings?.followSystem ?: true) }
                
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "الوضع الليلي",
                    subtitle = "تفعيل الوضع الليلي",
                    checked = darkModeEnabled && !followSystemEnabled,
                    onCheckedChange = { 
                        darkModeEnabled = it
                        if (it) followSystemEnabled = false
                        viewModel.updateDarkMode(context, it)
                    }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "اتباع نظام الجهاز",
                    subtitle = "تغيير الوضع حسب إعدادات الجهاز",
                    checked = followSystemEnabled,
                    onCheckedChange = { 
                        followSystemEnabled = it
                        if (it) darkModeEnabled = false
                        viewModel.updateFollowSystem(context, it)
                    }
                )
            }

            // Privacy Section
            SettingsSection(title = "الخصوصية") {
                SettingsSwitchItem(
                    icon = Icons.Default.Fingerprint,
                    title = "قفل التطبيق بالبصمة",
                    subtitle = "حماية قائمة المحظورين ببصمة الإصبع",
                    checked = settings?.biometricLock ?: false,
                    onCheckedChange = { viewModel.updateBiometricLock(context, it) }
                )
            }

            // Backup Section
            SettingsSection(title = "النسخ الاحتياطي") {
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
                        Text("تصدير القائمة")
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
                    Text("استيراد القائمة")
                }
            }

            // Danger Zone
            SettingsSection(title = "منطقة الخطر") {
                Button(
                    onClick = { viewModel.clearAllData(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlockRed)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مسح جميع البيانات")
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
