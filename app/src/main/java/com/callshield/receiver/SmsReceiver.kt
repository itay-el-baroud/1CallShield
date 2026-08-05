package com.callshield.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.callshield.MainActivity
import com.callshield.R
import com.callshield.data.AppDatabase
import com.callshield.data.CallLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    
    private val CHANNEL_ID = "callshield_blocked_sms"
    private val CHANNEL_NAME = "الرسائل المحظورة"
    
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
                logBlockedSms(context, db, phoneNumber, blocked.displayName, messageBody)
                showBlockedSmsNotification(context, phoneNumber, blocked.displayName, messageBody)
                return@launch
            }

            // Block if message contains spam keywords
            if (containsSpamKeywords(messageBody)) {
                logBlockedSms(context, db, phoneNumber, "", messageBody)
                showBlockedSmsNotification(context, phoneNumber, "", messageBody)
            }
        }
    }

    private fun containsSpamKeywords(message: String): Boolean {
        val lowerMessage = message.lowercase()
        return spamKeywords.any { keyword ->
            lowerMessage.contains(keyword.lowercase())
        }
    }

    private suspend fun logBlockedSms(context: Context, db: AppDatabase, phoneNumber: String, displayName: String, messageBody: String) {
        val log = CallLog(
            phoneNumber = phoneNumber,
            displayName = displayName,
            callType = "blocked_sms",
            messageContent = messageBody
        )
        db.callLogDao().insert(log)
    }

    private fun showBlockedSmsNotification(context: Context, phoneNumber: String, displayName: String, messageBody: String) {
        createNotificationChannel(context)
        
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale("ar")).format(Date())
        val name = if (displayName.isNotEmpty()) displayName else phoneNumber
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_sms", phoneNumber)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("رسالة محظورة من $name")
            .setContentText("$timeStr - $messageBody")
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الرسائل المحظورة"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
