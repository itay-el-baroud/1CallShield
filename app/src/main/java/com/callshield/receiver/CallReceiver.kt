package com.callshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.callshield.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CallReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return
        
        Log.d(TAG, "Call state: $state, Number: $phoneNumber")
        
        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            checkAndBlockCall(context, phoneNumber)
        }
    }
    
    private fun checkAndBlockCall(context: Context, phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val blockedNumber = db.blockedNumberDao().getByNumber(phoneNumber)
                
                if (blockedNumber != null && blockedNumber.blockCalls) {
                    Log.d(TAG, "Blocking call from: $phoneNumber")
                    
                    // Log the blocked call
                    db.callLogDao().insert(
                        com.callshield.data.CallLog(
                            phoneNumber = phoneNumber,
                            displayName = blockedNumber.displayName ?: phoneNumber,
                            callType = "BLOCKED",
                            timestamp = System.currentTimeMillis(),
                            duration = 0,
                            simSlot = "unknown",
                            messageContent = null
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking call: ${e.message}")
            }
        }
    }
}
