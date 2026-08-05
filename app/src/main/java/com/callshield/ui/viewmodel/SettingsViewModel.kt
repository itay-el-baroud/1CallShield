package com.callshield.ui.viewmodel

import android.content.Context
import android.os.Environment
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.data.AppDatabase
import com.callshield.utils.AccessibilityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val autoBlockSpam: Boolean,
    val autoBlockAfterAttempts: Boolean,
    val attemptThreshold: Int,
    val blockUnknownOnly: Boolean,
    val darkMode: Boolean,
    val followSystem: Boolean,
    val emergencyBypass: Boolean,
    val keywordBypass: Boolean,
    val fakeDisconnect: Boolean,
    val smsKeywordFilter: Boolean,
    val biometricLock: Boolean,
    val blockAllSims: Boolean
)

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow<AppSettings?>(null)
    val settings: StateFlow<AppSettings?> = _settings

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled

    private object PreferencesKeys {
        val AUTO_BLOCK_SPAM = booleanPreferencesKey("auto_block_spam")
        val AUTO_BLOCK_ATTEMPTS = booleanPreferencesKey("auto_block_attempts")
        val ATTEMPT_THRESHOLD = intPreferencesKey("attempt_threshold")
        val BLOCK_UNKNOWN_ONLY = booleanPreferencesKey("block_unknown_only")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
        val EMERGENCY_BYPASS = booleanPreferencesKey("emergency_bypass")
        val KEYWORD_BYPASS = booleanPreferencesKey("keyword_bypass")
        val FAKE_DISCONNECT = booleanPreferencesKey("fake_disconnect")
        val SMS_KEYWORD_FILTER = booleanPreferencesKey("sms_keyword_filter")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val BLOCK_ALL_SIMS = booleanPreferencesKey("block_all_sims")
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
                followSystem = prefs[PreferencesKeys.FOLLOW_SYSTEM] ?: true,
                emergencyBypass = prefs[PreferencesKeys.EMERGENCY_BYPASS] ?: false,
                keywordBypass = prefs[PreferencesKeys.KEYWORD_BYPASS] ?: false,
                fakeDisconnect = prefs[PreferencesKeys.FAKE_DISCONNECT] ?: false,
                smsKeywordFilter = prefs[PreferencesKeys.SMS_KEYWORD_FILTER] ?: false,
                biometricLock = prefs[PreferencesKeys.BIOMETRIC_LOCK] ?: false,
                blockAllSims = prefs[PreferencesKeys.BLOCK_ALL_SIMS] ?: true
            )
            _isAccessibilityEnabled.value = AccessibilityUtils.isServiceEnabled(context)
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

    fun updateEmergencyBypass(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.EMERGENCY_BYPASS] = value }
            loadSettings(context)
        }
    }

    fun updateKeywordBypass(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.KEYWORD_BYPASS] = value }
            loadSettings(context)
        }
    }

    fun updateFakeDisconnect(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.FAKE_DISCONNECT] = value }
            loadSettings(context)
        }
    }

    fun updateSmsKeywordFilter(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.SMS_KEYWORD_FILTER] = value }
            loadSettings(context)
        }
    }

    fun updateBiometricLock(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_LOCK] = value }
            loadSettings(context)
        }
    }

    fun updateBlockAllSims(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[PreferencesKeys.BLOCK_ALL_SIMS] = value }
            loadSettings(context)
        }
    }

    fun enableAccessibilityService(context: Context) {
        AccessibilityUtils.requestEnableService(context)
        viewModelScope.launch {
            _isAccessibilityEnabled.value = AccessibilityUtils.isServiceEnabled(context)
        }
    }

    fun openAccessibilitySettings(context: Context) {
        AccessibilityUtils.openAccessibilitySettings(context)
    }

    fun exportBlockedList(context: Context) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val blockedNumbers = db.blockedNumberDao().getAll()
            
            val jsonArray = JSONArray()
            blockedNumbers.forEach { number ->
                val jsonObject = JSONObject().apply {
                    put("phoneNumber", number.phoneNumber)
                    put("displayName", number.displayName)
                    put("label", number.label)
                    put("category", number.category)
                    put("blockCalls", number.blockCalls)
                    put("blockSms", number.blockSms)
                    put("simSlot", number.simSlot)
                    put("simOperator", number.simOperator)
                    put("createdAt", number.createdAt)
                }
                jsonArray.put(jsonObject)
            }
            
            val fileName = "callshield_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.write(jsonArray.toString(2))
            }
        }
    }

    fun importBlockedList(context: Context) {
        viewModelScope.launch {
            // Implementation for importing would go here
        }
    }

    fun clearAllData(context: Context) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            db.blockedNumberDao().getAll().forEach {
                db.blockedNumberDao().delete(it)
            }
            db.callLogDao().getAll().forEach {
                db.callLogDao().deleteById(it.id)
            }
        }
    }
}
