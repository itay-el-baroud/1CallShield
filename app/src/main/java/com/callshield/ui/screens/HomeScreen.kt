package com.callshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callshield.data.BlockedNumber
import com.callshield.ui.theme.BlockRed
import com.callshield.ui.theme.WarningOrange
import com.callshield.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val blockedNumbers by viewModel.blockedNumbers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pinnedNumbers by viewModel.pinnedNumbers.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<BlockedNumber?>(null) }
    var showAnalysisDialog by remember { mutableStateOf<BlockedNumber?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBlockedNumbers(context)
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("ابحث في الأرقام المحظورة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Block,
                        value = blockedNumbers.size.toString(),
                        label = "محظور"
                    )
                    StatItem(
                        icon = Icons.Default.Phone,
                        value = blockedNumbers.sumOf { it.callAttempts }.toString(),
                        label = "محاولات"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pinned Numbers Section
            if (pinnedNumbers.isNotEmpty()) {
                Text(
                    "الأرقام الأكثر إزعاجاً",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = WarningOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pinnedNumbers) { number ->
                        BlockedNumberCard(
                            number = number,
                            onDelete = { showDeleteDialog = number },
                            onAnalysis = { showAnalysisDialog = number },
                            isPinned = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Blocked Numbers List
            if (blockedNumbers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لا توجد أرقام محظورة بعد",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedNumbers) { number ->
                        BlockedNumberCard(
                            number = number,
                            onDelete = { showDeleteDialog = number },
                            onAnalysis = { showAnalysisDialog = number },
                            isPinned = false
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { number ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("إلغاء الحظر") },
            text = { Text("هل أنت متأكد من إلغاء حظر ${number.phoneNumber}؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.unblockNumber(context, number)
                        showDeleteDialog = null
                    }
                ) {
                    Text("إلغاء الحظر", color = BlockRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Analysis Dialog
    showAnalysisDialog?.let { number ->
        AlertDialog(
            onDismissRequest = { showAnalysisDialog = null },
            title = { Text("تحليل الإزعاج") },
            text = {
                Column {
                    Text("الرقم: ${number.phoneNumber}")
                    Text("عدد المحاولات: ${number.callAttempts}")
                    Text("آخر محاولة: ${if (number.lastAttemptTime != null) "منذ قليل" else "لا يوجد"}")
                    if (number.callAttempts > 10) {
                        Text(
                            "هذا الرقم مزعج جداً! تحب تحظره نهائياً؟",
                            color = BlockRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAnalysisDialog = null }) {
                    Text("فهمت")
                }
            }
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun BlockedNumberCard(
    number: BlockedNumber,
    onDelete: () -> Unit,
    onAnalysis: () -> Unit,
    isPinned: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPinned) 4.dp else 2.dp),
        colors = if (isPinned) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = number.phoneNumber,
                    style = MaterialTheme.typography.titleMedium
                )
                if (number.displayName.isNotEmpty()) {
                    Text(
                        text = number.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (number.label.isNotEmpty()) {
                    Text(
                        text = number.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (number.callAttempts > 0) {
                    Text(
                        text = "${number.callAttempts} محاولات",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPinned) BlockRed else WarningOrange
                    )
                }
            }
            IconButton(onClick = onAnalysis) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "تحليل",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "إلغاء الحظر",
                    tint = BlockRed
                )
            }
        }
    }
}
