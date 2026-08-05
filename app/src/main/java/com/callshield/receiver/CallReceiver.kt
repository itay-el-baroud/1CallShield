package com.callshield.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.telephony.TelephonyManager
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

class CallReceiver : BroadcastReceiver() {
    
    private val CHANNEL_ID = "callshield_blocked_calls"
    private val CHANNEL_NAME = "المكالمات المحظورة"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val simSlot = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.getIntExtra("android.telecom.extra.SIM_SLOT_INDEX", -1)
            } else -1

            if (state == TelephonyManager.EXTRA_STATE_RINGING && number != null) {
                checkAndBlockCall(context, number, simSlot)
            }
        }
    }

    private fun checkAndBlockCall(context: Context, phoneNumber: String, simSlot: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val blocked = db.blockedNumberDao().getByNumber(phoneNumber)

            if (blocked != null && blocked.blockCalls) {
                // Check if SIM slot matches (-1 means all SIMs)
                if (blocked.simSlot != -1 && blocked.simSlot != simSlot) {
                    return@launch // Don't block if SIM doesn't match
                }

                // Check emergency bypass
                val recentAttempts = db.callLogDao().getAttemptsSince(
                    phoneNumber,
                    System.currentTimeMillis() - blocked.bypassTimeWindow
                )
                
                if (blocked.emergencyBypass && recentAttempts >= blocked.maxAttemptsBeforeBypass) {
                    return@launch // Allow emergency call
                }

                // Play fake disconnect sound
                playFakeDisconnectSound(context)

                // Log the blocked call
                val currentTime = System.currentTimeMillis()
                val log = CallLog(
                    phoneNumber = phoneNumber,
                    displayName = blocked.displayName,
                    callType = "blocked_call",
                    timestamp = currentTime
                )
                db.callLogDao().insert(log)

                // Increment attempts
                db.blockedNumberDao().incrementAttempts(
                    phoneNumber,
                    currentTime
                )

                // Show notification
                showBlockedCallNotification(context, phoneNumber, blocked.displayName, currentTime)
            } else {
                // Not blocked - show normal notification
                val currentTime = System.currentTimeMillis()
                showNormalCallNotification(context, phoneNumber, currentTime)
            }
        }
    }

    private fun playFakeDisconnectSound(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_CALL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBlockedCallNotification(context: Context, phoneNumber: String, displayName: String, time: Long) {
        createNotificationChannel(context)
        
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale("ar")).format(Date(time))
        val name = if (displayName.isNotEmpty()) displayName else phoneNumber
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("مكالمة محظورة")
            .setContentText("الرقم $name حاول الاتصال بك في تمام $timeStr")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showNormalCallNotification(context: Context, phoneNumber: String, time: Long) {
        createNotificationChannel(context)
        
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale("ar")).format(Date(time))
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("مكالمة واردة")
            .setContentText("الرقم $phoneNumber اتصل بك في تمام $timeStr")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
                description = "إشعارات المكالمات المحظورة"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
