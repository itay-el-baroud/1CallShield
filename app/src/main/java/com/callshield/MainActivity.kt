package com.callshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.callshield.ui.screens.DialerScreen
import com.callshield.ui.screens.HomeScreen
import com.callshield.ui.screens.LogsScreen
import com.callshield.ui.screens.SettingsScreen
import com.callshield.ui.theme.CallShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "dialer"
                    ) {
                        composable("dialer") {
                            DialerScreen(
                                onNavigateToBlocked = { navController.navigate("home") },
                                onNavigateToLogs = { navController.navigate("logs") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        
                        composable("home") {
                            HomeScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("logs") {
                            LogsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
