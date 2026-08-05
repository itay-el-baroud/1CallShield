package com.callshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun DialerScreen(
    onNavigateToBlocked: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToBlocked) {
                Icon(Icons.Default.Block, contentDescription = "محظورين")
            }
            Text(
                "CallShield",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "إعدادات")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Phone Number Display
        Text(
            text = phoneNumber.ifEmpty { " " },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Delete Button
        if (phoneNumber.isNotEmpty()) {
            IconButton(
                onClick = { phoneNumber = phoneNumber.dropLast(1) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "مسح")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("*", "0", "#")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        DialerKey(
                            key = key,
                            onClick = { phoneNumber += key }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call Button
        FloatingActionButton(
            onClick = {
                if (phoneNumber.isNotEmpty()) {
                    isLoading = true
                    val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                        data = "tel:$phoneNumber".toUri()
                    }
                    context.startActivity(intent)
                    isLoading = false
                }
            },
            shape = CircleShape,
            containerColor = Color(0xFF4CAF50),
            modifier = Modifier.size(72.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "اتصال",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Default.History,
                label = "السجلات",
                onClick = onNavigateToLogs
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "جهات الاتصال",
                onClick = { /* TODO */ }
            )
            BottomNavItem(
                icon = Icons.Default.Voicemail,
                label = "البريد الصوتي",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun DialerKey(
    key: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
