package com.callshield.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.callshield.ui.theme.CallShieldTheme

class InCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val phoneNumber = intent.getStringExtra("phone_number") ?: "Unknown"
        val displayName = intent.getStringExtra("display_name") ?: phoneNumber
        
        setContent {
            CallShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InCallScreen(
                        phoneNumber = phoneNumber,
                        displayName = displayName,
                        onEndCall = { finish() },
                        onBlockNumber = { number ->
                            // TODO: Block number logic
                            finish()
                        }
                    )
                }
            }
        }
    }
}
