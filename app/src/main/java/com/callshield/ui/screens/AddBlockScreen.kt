package com.callshield.ui.screens

import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.callshield.ui.theme.BlockRed
import com.callshield.ui.viewmodel.AddBlockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlockScreen(viewModel: AddBlockViewModel = viewModel()) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var selectedSim by remember { mutableStateOf(-1) } // -1 = all SIMs
    var showSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Get SIM info
    val simCards = remember { getSimCards(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حظر رقم جديد") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phone Icon
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("رقم الهاتف") },
                placeholder = { Text("مثال: 01234567890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SIM Selection
            Text(
                "اختر الخط",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // All SIMs option
            FilterChip(
                selected = selectedSim == -1,
                onClick = { selectedSim = -1 },
                label = { Text("كل الخطوط") },
                leadingIcon = if (selectedSim == -1) {
                    { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            // Individual SIMs
            simCards.forEach { sim ->
                FilterChip(
                    selected = selectedSim == sim.slotIndex,
                    onClick = { selectedSim = sim.slotIndex },
                    label = { Text("${sim.displayName} - ${sim.carrierName}") },
                    leadingIcon = if (selectedSim == sim.slotIndex) {
                        { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading Indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Block Button
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        isLoading = true
                        viewModel.blockNumber(
                            context = context,
                            phoneNumber = phoneNumber,
                            displayName = "",
                            label = "",
                            category = "spam",
                            blockCalls = true,
                            blockSms = true,
                            isTemporary = false,
                            tempDuration = null,
                            simSlot = selectedSim,
                            simOperator = simCards.find { it.slotIndex == selectedSim }?.carrierName ?: "",
                            networkType = ""
                        )
                        isLoading = false
                        showSuccess = true
                        phoneNumber = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlockRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Block, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "حظر الرقم",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Success Message
            if (showSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "تم حظر الرقم بنجاح!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

data class SimCardInfo(
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String
)

fun getSimCards(context: Context): List<SimCardInfo> {
    val simCards = mutableListOf<SimCardInfo>()
    
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
            
            activeSubscriptionInfoList?.forEach { info ->
                simCards.add(
                    SimCardInfo(
                        slotIndex = info.simSlotIndex,
                        displayName = info.displayName?.toString() ?: "خط ${info.simSlotIndex + 1}",
                        carrierName = info.carrierName?.toString() ?: "غير معروف",
                        number = info.number?.toString() ?: ""
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // If no SIMs detected, add default
    if (simCards.isEmpty()) {
        simCards.add(SimCardInfo(0, "خط 1", "غير معروف", ""))
        simCards.add(SimCardInfo(1, "خط 2", "غير معروف", ""))
    }
    
    return simCards
}
