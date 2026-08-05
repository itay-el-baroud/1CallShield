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
    val createdAt: Long = System.currentTimeMillis()
)
