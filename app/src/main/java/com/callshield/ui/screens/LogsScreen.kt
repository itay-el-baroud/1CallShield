package com.callshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callshield.data.CallLog
import com.callshield.ui.theme.BlockRed
import com.callshield.ui.theme.InfoBlue
import com.callshield.ui.viewmodel.LogsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(viewModel: LogsViewModel = viewModel()) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLogs(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المكالمات") },
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
            // Stats Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "تحليل",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    stats?.let { s ->
                        Text("إجمالي المكالمات المحظورة: ${s.totalCalls}")
                        Text("إجمالي الرسائل المحظورة: ${s.totalSms}")
                        Text("أكثر رقم نشاطاً: ${s.mostActiveNumber ?: "لا يوجد"}")
                        Text("آخر 24 ساعة: ${s.last24Hours} محاولات")
                    }
                }
            }

            // Logs List
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لا يوجد سجلات بعد",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        LogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: CallLog) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("ar"))
    val dateStr = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (log.callType == "blocked_call") Icons.Default.Call else Icons.Default.Message,
                contentDescription = null,
                tint = if (log.callType == "blocked_call") BlockRed else InfoBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.phoneNumber,
                    style = MaterialTheme.typography.titleSmall
                )
                if (log.displayName.isNotEmpty()) {
                    Text(
                        text = log.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!log.isRead) {
                Badge(
                    containerColor = BlockRed
                ) {
                    Text("جديد")
                }
            }
        }
    }
}
