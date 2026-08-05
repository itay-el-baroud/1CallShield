package com.callshield.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.data.AppDatabase
import com.callshield.data.CallLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LogStats(
    val totalCalls: Int,
    val totalSms: Int,
    val mostActiveNumber: String?,
    val last24Hours: Int
)

class LogsViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<CallLog>>(emptyList())
    val logs: StateFlow<List<CallLog>> = _logs

    private val _stats = MutableStateFlow<LogStats?>(null)
    val stats: StateFlow<LogStats?> = _stats

    fun loadLogs(context: Context) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val allLogs = db.callLogDao().getAll()
            _logs.value = allLogs
            
            // Calculate stats
            val calls = allLogs.count { it.callType == "blocked_call" }
            val sms = allLogs.count { it.callType == "blocked_sms" }
            val last24h = allLogs.count { 
                it.timestamp > System.currentTimeMillis() - (24 * 60 * 60 * 1000) 
            }
            val mostActive = allLogs
                .groupBy { it.phoneNumber }
                .maxByOrNull { it.value.size }
                ?.key
            
            _stats.value = LogStats(calls, sms, mostActive, last24h)
        }
    }
}
