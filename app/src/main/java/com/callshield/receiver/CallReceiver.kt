package com.callshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.callshield.data.AppDatabase
import com.callshield.data.CallLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING && number != null) {
                checkAndBlockCall(context, number)
            }
        }
    }

    private fun checkAndBlockCall(context: Context, phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val blocked = db.blockedNumberDao().getByNumber(phoneNumber)

            if (blocked != null && blocked.blockCalls) {
                // Log the blocked call
                val log = CallLog(
                    phoneNumber = phoneNumber,
                    displayName = blocked.displayName,
                    callType = "blocked_call"
                )
                db.callLogDao().insert(log)

                // Increment attempts
                db.blockedNumberDao().incrementAttempts(
                    phoneNumber,
                    System.currentTimeMillis()
                )
            }
        }
    }
}
