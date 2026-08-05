package com.callshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.android.internal.telephony.ITelephony
import com.callshield.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method

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
            // Incoming call - check if blocked
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
                    
                    // Block using ITelephony (Reflection)
                    blockCallWithITelephony(context)
                    
                    // Log the blocked call
                    db.callLogDao().insert(
                        com.callshield.data.model.CallLog(
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
    
    private fun blockCallWithITelephony(context: Context) {
        try {
            val telephonyService = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            // Use reflection to access ITelephony
            val telephonyClass = Class.forName(telephonyService.javaClass.name)
            val method: Method = telephonyClass.getDeclaredMethod("getITelephony")
            method.isAccessible = true
            
            val telephonyInterface = method.invoke(telephonyService)
            val telephonyInterfaceClass = Class.forName(telephonyInterface.javaClass.name)
            val endCallMethod: Method = telephonyInterfaceClass.getDeclaredMethod("endCall")
            
            endCallMethod.invoke(telephonyInterface)
            Log.d(TAG, "Call ended via ITelephony")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to block call via ITelephony: ${e.message}")
        }
    }
}
