package com.callshield.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val displayName: String = "",
    val callType: String = "blocked",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Int = 0,
    val isRead: Boolean = false
)
