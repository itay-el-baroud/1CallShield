package com.callshield.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.data.BlockedNumber
import com.callshield.data.AppDatabase
import kotlinx.coroutines.launch

class AddBlockViewModel : ViewModel() {
    fun blockNumber(
        context: Context,
        phoneNumber: String,
        displayName: String,
        label: String,
        category: String,
        blockCalls: Boolean,
        blockSms: Boolean,
        isTemporary: Boolean,
        tempDuration: String?
    ) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            
            val unblockTime = if (isTemporary) {
                val currentTime = System.currentTimeMillis()
                when (tempDuration) {
                    "1 hour" -> currentTime + (60 * 60 * 1000)
                    "1 day" -> currentTime + (24 * 60 * 60 * 1000)
                    "1 week" -> currentTime + (7 * 24 * 60 * 60 * 1000)
                    else -> null
                }
            } else null

            val blockedNumber = BlockedNumber(
                phoneNumber = phoneNumber,
                displayName = displayName,
                label = label,
                category = category,
                blockCalls = blockCalls,
                blockSms = blockSms,
                isTemporary = isTemporary,
                unblockTime = unblockTime
            )

            db.blockedNumberDao().insert(blockedNumber)
        }
    }
}
