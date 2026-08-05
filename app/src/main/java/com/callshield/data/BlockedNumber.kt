package com.callshield.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val displayName: String = "",
    val label: String = "",
    val category: String = "spam",
    val blockCalls: Boolean = true,
    val blockSms: Boolean = true,
    val isTemporary: Boolean = false,
    val unblockTime: Long? = null,
    val callAttempts: Int = 0,
    val lastAttemptTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val emergencyBypass: Boolean = false,
    val bypassKeyword: String = "طوارئ",
    val maxAttemptsBeforeBypass: Int = 3,
    val bypassTimeWindow: Long = 120000,
    val simSlot: Int = -1, // -1 means all SIMs, 0 = SIM1, 1 = SIM2
    val simOperator: String = "",
    val networkType: String = ""
)
