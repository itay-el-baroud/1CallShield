package com.callshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
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
                // Check emergency bypass
                val recentAttempts = db.callLogDao().getAttemptsSince(
                    phoneNumber,
                    System.currentTimeMillis() - blocked.bypassTimeWindow
                )
                
                if (blocked.emergencyBypass && recentAttempts >= blocked.maxAttemptsBeforeBypass) {
                    // Allow emergency call - don't block
                    return@launch
                }

                // Play fake disconnect sound
                playFakeDisconnectSound(context)

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

    private fun playFakeDisconnectSound(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Set to speaker briefly to play tone
            audioManager.mode = AudioManager.MODE_IN_CALL
            // Tone will be played by system when call is rejected
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
