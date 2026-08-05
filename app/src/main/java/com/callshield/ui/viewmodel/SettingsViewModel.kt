package com.callshield.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val autoBlockSpam: Boolean,
    val autoBlockAfterAttempts: Boolean,
    val attemptThreshold: Int,
    val blockUnknownOnly: Boolean,
    val darkMode: Boolean,
    val followSystem: Boolean
)

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow<AppSettings?>(null)
    val settings: StateFlow<AppSettings?> = _settings

    private object PreferencesKeys {
        val AUTO_BLOCK_SPAM = booleanPreferencesKey("auto_block_spam")
        val AUTO_BLOCK_ATTEMPTS = booleanPreferencesKey("auto_block_attempts")
        val ATTEMPT_THRESHOLD = intPreferencesKey("attempt_threshold")
        val BLOCK_UNKNOWN_ONLY = booleanPreferencesKey("block_unknown_only")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
    }

    fun loadSettings(context: Context) {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            _settings.value = AppSettings(
                autoBlockSpam = prefs[PreferencesKeys.AUTO_BLOCK_SPAM] ?: false,
                autoBlockAfterAttempts = prefs[PreferencesKeys.AUTO_BLOCK_ATTEMPTS] ?: false,
                attemptThreshold = prefs[PreferencesKeys.ATTEMPT_THRESHOLD] ?: 5,
                blockUnknownOnly = prefs[PreferencesKeys.BLOCK_UNKNOWN_ONLY] ?: false,
                darkMode = prefs[PreferencesKeys.DARK_MODE] ?: false,
                followSystem = prefs[PreferencesKeys.FOLLOW_SYSTEM] ?: true
            )
        }
    }

    fun updateAutoBlockSpam(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.AUTO_BLOCK_SPAM] = value }
            loadSettings(context)
        }
    }

    fun updateAutoBlockAfterAttempts(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.AUTO_BLOCK_ATTEMPTS] = value }
            loadSettings(context)
        }
    }

    fun updateAttemptThreshold(context: Context, value: Int) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.ATTEMPT_THRESHOLD] = value }
            loadSettings(context)
        }
    }

    fun updateBlockUnknownOnly(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.BLOCK_UNKNOWN_ONLY] = value }
            loadSettings(context)
        }
    }

    fun updateDarkMode(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { 
                it[PreferencesKeys.DARK_MODE] = value 
                if (value) it[PreferencesKeys.FOLLOW_SYSTEM] = false
            }
            loadSettings(context)
        }
    }

    fun updateFollowSystem(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { 
                it[PreferencesKeys.FOLLOW_SYSTEM] = value 
                if (value) it[PreferencesKeys.DARK_MODE] = false
            }
            loadSettings(context)
        }
    }

    fun clearAllData(context: Context) {
        viewModelScope.launch {
            val db = com.callshield.data.AppDatabase.getDatabase(context)
            db.blockedNumberDao().getAll().forEach {
                db.blockedNumberDao().delete(it)
            }
            db.callLogDao().getAll().forEach {
                db.callLogDao().deleteById(it.id)
            }
        }
    }
}
