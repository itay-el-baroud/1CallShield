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
    
    private val spamKeywords = listOf(
        "خصم", "عروض", "كود", "اشترك", "مجانا", "هدية",
        "فاز", "ربح", "عرض", "تخفيض", "discount", "offer",
        "free", "win", "promo", "sale", "buy"
    )
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val originatingAddress = messages?.firstOrNull()?.originatingAddress
            val messageBody = messages?.joinToString("") { it.messageBody ?: "" } ?: ""

            if (originatingAddress != null) {
                checkAndBlockSms(context, originatingAddress, messageBody)
            }
        }
    }

    private fun checkAndBlockSms(context: Context, phoneNumber: String, messageBody: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val blocked = db.blockedNumberDao().getByNumber(phoneNumber)

            // Block if number is in blacklist
            if (blocked != null && blocked.blockSms) {
                logBlockedSms(context, db, phoneNumber, blocked.displayName)
                return@launch
            }

            // Block if message contains spam keywords
            if (containsSpamKeywords(messageBody)) {
                logBlockedSms(context, db, phoneNumber, "")
            }
        }
    }

    private fun containsSpamKeywords(message: String): Boolean {
        val lowerMessage = message.lowercase()
        return spamKeywords.any { keyword ->
            lowerMessage.contains(keyword.lowercase())
        }
    }

    private fun logBlockedSms(context: Context, db: AppDatabase, phoneNumber: String, displayName: String) {
        val log = CallLog(
            phoneNumber = phoneNumber,
            displayName = displayName,
            callType = "blocked_sms"
        )
        db.callLogDao().insert(log)
    }
}
