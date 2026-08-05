package com.callshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callshield.ui.theme.BlockRed
import com.callshield.ui.viewmodel.AddBlockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlockScreen(viewModel: AddBlockViewModel = viewModel()) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("spam") }
    var blockCalls by remember { mutableStateOf(true) }
    var blockSms by remember { mutableStateOf(true) }
    var isTemporary by remember { mutableStateOf(false) }
    var tempDuration by remember { mutableStateOf("1 hour") }
    var showSuccess by remember { mutableStateOf(false) }

    val categories = listOf("spam", "harassment", "work", "personal", "unknown")
    val tempOptions = listOf("1 hour", "1 day", "1 week")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Number") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phone Number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Display Name (Optional)
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Label (Optional)
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category
            Text("Category", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Block Options
            Text("Block Options", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = blockCalls,
                        onCheckedChange = { blockCalls = it }
                    )
                    Text("Calls")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = blockSms,
                        onCheckedChange = { blockSms = it }
                    )
                    Text("SMS")
                }
            }

            // Temporary Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTemporary,
                    onCheckedChange = { isTemporary = it }
                )
                Text("Temporary Block")
            }

            if (isTemporary) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tempOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = tempOptions.size
                            ),
                            onClick = { tempDuration = option },
                            selected = tempDuration == option
                        ) {
                            Text(option)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        viewModel.blockNumber(
                            context = context,
                            phoneNumber = phoneNumber,
                            displayName = displayName,
                            label = label,
                            category = selectedCategory,
                            blockCalls = blockCalls,
                            blockSms = blockSms,
                            isTemporary = isTemporary,
                            tempDuration = if (isTemporary) tempDuration else null
                        )
                        showSuccess = true
                        phoneNumber = ""
                        displayName = ""
                        label = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BlockRed)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Block Number")
            }
        }

        // Success Snackbar
        if (showSuccess) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSuccess = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text("Number blocked successfully!")
            }
        }
    }
}
