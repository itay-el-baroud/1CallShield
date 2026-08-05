package com.callshield.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.util.Log
import com.callshield.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallShieldScreeningService : CallScreeningService() {
    
    companion object {
        private const val TAG = "CallShieldScreening"
    }
    
    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle.schemeSpecificPart
        
        Log.d(TAG, "Screening call from: $phoneNumber")
        
        CoroutineScope(Dispatchers.IO).launch {
            val response = CallResponse.Builder()
            
            try {
                val db = AppDatabase.getDatabase(this@CallShieldScreeningService)
                val blockedNumber = db.blockedNumberDao().getByNumber(phoneNumber)
                
                if (blockedNumber != null && blockedNumber.blockCalls) {
                    Log.d(TAG, "Blocking call from: $phoneNumber")
                    
                    // Block the call completely
                    response.apply {
                        setDisallowCall(true)
                        setRejectCall(true)
                        setSkipCallLog(false) // Keep log for records
                        setSkipNotification(false)
                    }
                    
                    // Log the blocked call
                    db.callLogDao().insert(
                        com.callshield.data.model.CallLog(
                            phoneNumber = phoneNumber,
                            displayName = blockedNumber.displayName ?: phoneNumber,
                            callType = "BLOCKED",
                            timestamp = System.currentTimeMillis(),
                            duration = 0,
                            simSlot = callDetails.accountHandle?.id ?: "unknown",
                            messageContent = null
                        )
                    )
                } else {
                    // Allow the call
                    response.setDisallowCall(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error screening call: ${e.message}")
                response.setDisallowCall(false) // Allow on error
            }
            
            respondToCall(callDetails, response.build())
        }
    }
}
