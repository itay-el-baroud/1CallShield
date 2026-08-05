package com.callshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.callshield.ui.components.BottomNavBar
import com.callshield.ui.screens.AddBlockScreen
import com.callshield.ui.screens.HomeScreen
import com.callshield.ui.screens.LogsScreen
import com.callshield.ui.screens.SettingsScreen
import com.callshield.ui.theme.CallShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallShieldTheme {
                val navController = rememberNavController()
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
