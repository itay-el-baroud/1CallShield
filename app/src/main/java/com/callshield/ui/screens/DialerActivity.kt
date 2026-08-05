package com.callshield.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.callshield.ui.theme.CallShieldTheme

class DialerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val phoneNumber = intent.data?.schemeSpecificPart ?: ""
        
        setContent {
            CallShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DialerScreen(
                        initialNumber = phoneNumber,
                        onNavigateToBlocked = { finish() },
                        onNavigateToLogs = { finish() },
                        onNavigateToSettings = { finish() }
                    )
                }
            }
        }
    }
}

