package com.callshield.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.data.BlockedNumber
import com.callshield.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _blockedNumbers = MutableStateFlow<List<BlockedNumber>>(emptyList())
    val blockedNumbers: StateFlow<List<BlockedNumber>> = _blockedNumbers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun loadBlockedNumbers(context: Context) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val numbers = db.blockedNumberDao().getAll()
            _blockedNumbers.value = numbers
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Filter logic would go here
    }

    fun unblockNumber(context: Context, number: BlockedNumber) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            db.blockedNumberDao().delete(number)
            loadBlockedNumbers(context)
        }
    }
}
