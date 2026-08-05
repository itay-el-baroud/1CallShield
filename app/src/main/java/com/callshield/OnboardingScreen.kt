package com.callshield

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    var permissionsGranted by remember { mutableStateOf(false) }
    
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.entries.any { it.value }
        if (permissionsGranted) {
            currentPage = 2
        }
    }
    
    LaunchedEffect(Unit) {
        permissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (permissionsGranted) {
            currentPage = 2
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CallShield") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentPage) {
                0 -> WelcomePage(
                    onNext = { currentPage = 1 }
                )
                1 -> PermissionsPage(
                    onGrantPermissions = {
                        permissionLauncher.launch(requiredPermissions)
                    },
                    onSkip = { currentPage = 2 }
                )
                2 -> TourPage(
                    onFinish = onPermissionsGranted
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "مرحباً بك في CallShield",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "حمايتك من المكالمات والرسائل المزعجة",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ابدأ")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun PermissionsPage(
    onGrantPermissions: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "الأذونات المطلوبة",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "يحتاج التطبيق إلى بعض الأذونات للعمل بشكل صحيح",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission items
        PermissionItem(
            icon = Icons.Default.Phone,
            title = "المكالمات",
            description = "حظر المكالمات الواردة"
        )
        PermissionItem(
            icon = Icons.Default.Message,
            title = "الرسائل",
            description = "حظر الرسائل المزعجة"
        )
        PermissionItem(
            icon = Icons.Default.Contacts,
            title = "جهات الاتصال",
            description = "التحقق من الأرقام المعروفة"
        )
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "الإشعارات",
            description = "إشعارات المحاولات المحظورة"
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onGrantPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("منح الأذونات")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تخطي")
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TourPage(
    onFinish: () -> Unit
) {
    var tourStep by remember { mutableStateOf(0) }
    
    val tourItems = listOf(
        TourItem(
            icon = Icons.Default.Home,
            title = "الرئيسية",
            description = "هنا يمكنك رؤية جميع الأرقام المحظورة وإحصائياتها"
        ),
        TourItem(
            icon = Icons.Default.Add,
            title = "إضافة حظر",
            description = "أضف رقماً جديداً للحظر مع خيارات متعددة"
        ),
        TourItem(
            icon = Icons.Default.History,
            title = "السجلات",
            description = "تابع جميع المحاولات المحظورة وتحليلاتها"
        ),
        TourItem(
            icon = Icons.Default.Settings,
            title = "الإعدادات",
            description = "خصص التطبيق حسب احتياجاتك"
        )
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "جولة تعريفية",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        AnimatedContent(
            targetState = tourStep,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            }
        ) { step ->
            val item = tourItems[step]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    item.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Dots indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            tourItems.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == tourStep) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Primary color dot
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (tourStep > 0) {
                TextButton(onClick = { tourStep-- }) {
                    Text("السابق")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            if (tourStep < tourItems.size - 1) {
                Button(onClick = { tourStep++ }) {
                    Text("التالي")
                }
            } else {
                Button(onClick = onFinish) {
                    Text("إنهاء")
                }
            }
        }
        
        TextButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تخطي الجولة")
        }
    }
}

data class TourItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)
