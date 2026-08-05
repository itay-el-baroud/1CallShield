package com.callshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.callshield.data.AppDatabase
import com.callshield.data.CallLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val originatingAddress = messages?.firstOrNull()?.originatingAddress

            if (originatingAddress != null) {
                checkAndBlockSms(context, originatingAddress)
            }
        }
    }

    private fun checkAndBlockSms(context: Context, phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val blocked = db.blockedNumberDao().getByNumber(phoneNumber)

            if (blocked != null && blocked.blockSms) {
                // Log the blocked SMS
                val log = CallLog(
                    phoneNumber = phoneNumber,
                    displayName = blocked.displayName,
                    callType = "blocked_sms"
                )
                db.callLogDao().insert(log)
            }
        }
    }
}
