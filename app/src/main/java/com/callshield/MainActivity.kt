package com.callshield

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.callshield.ui.components.BottomNavBar
import com.callshield.ui.screens.AddBlockScreen
import com.callshield.ui.screens.HomeScreen
import com.callshield.ui.screens.LogsScreen
import com.callshield.ui.screens.SettingsScreen
import com.callshield.ui.theme.CallShieldTheme
import java.util.concurrent.Executors

class MainActivity : FragmentActivity() {
    
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
    
    private var permissionsGranted by mutableStateOf(false)
    private var isAuthenticated by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.entries.any { it.value }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkPermissions()
        
        setContent {
            val navController = rememberNavController()
            var showOnboarding by remember { mutableStateOf(!permissionsGranted) }
            var showBiometric by remember { mutableStateOf(false) }
            
            val systemInDarkTheme = isSystemInDarkTheme()
            
            CallShieldTheme(
                darkTheme = systemInDarkTheme
            ) {
                when {
                    showOnboarding -> {
                        OnboardingScreen(
                            onPermissionsGranted = {
                                showOnboarding = false
                                permissionsGranted = true
                            }
                        )
                    }
                    showBiometric -> {
                        BiometricLockScreen(
                            onAuthenticated = {
                                showBiometric = false
                                isAuthenticated = true
                            }
                        )
                    }
                    else -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = { BottomNavBar(navController) }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable("home") { HomeScreen() }
                                composable("add") { AddBlockScreen() }
                                composable("logs") { LogsScreen() }
                                composable("settings") { SettingsScreen() }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun checkPermissions() {
        permissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun showBiometricPrompt() {
        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("قفل CallShield")
            .setSubtitle("استخدم بصمة إصبعك للدخول")
            .setNegativeButtonText("إلغاء")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}
