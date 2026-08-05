package com.callshield.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.callshield.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallBlockerService : AccessibilityService() {
    
    companion object {
        private const val TAG = "CallBlockerService"
        var isRunning = false
            private set
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.d(TAG, "Service connected")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(
                "com.android.incallui",
                "com.google.android.dialer",
                "com.samsung.android.incallui",
                "com.huawei.incallui"
            )
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val rootNode = rootInActiveWindow ?: return
            val phoneNumber = extractPhoneNumber(rootNode)
            
            if (phoneNumber != null) {
                checkAndBlockCall(phoneNumber)
            }
            
            rootNode.recycle()
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "Service destroyed")
    }
    
    private fun extractPhoneNumber(node: AccessibilityNodeInfo): String? {
        // Try to find phone number in the UI
        val text = node.text?.toString()
        if (text != null && isPhoneNumber(text)) {
            return text
        }
        
        // Search in children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val childNumber = extractPhoneNumber(child)
            child.recycle()
            if (childNumber != null) return childNumber
        }
        
        return null
    }
    
    private fun isPhoneNumber(text: String): Boolean {
        // Simple phone number detection
        val cleaned = text.replace(Regex("[^0-9+]"), "")
        return cleaned.length >= 8 && cleaned.length <= 15
    }
    
    private fun checkAndBlockCall(phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@CallBlockerService)
                val blocked = db.blockedNumberDao().getByNumber(phoneNumber)
                
                if (blocked != null && blocked.blockCalls) {
                    Log.d(TAG, "Blocking call from: $phoneNumber")
                    blockCall()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking call: ${e.message}")
            }
        }
    }
    
    private fun blockCall() {
        // Simulate pressing the end call button
        val rootNode = rootInActiveWindow ?: return
        
        // Find and click end call button
        val endCallButtons = rootNode.findAccessibilityNodeInfosByText("End call") +
                            rootNode.findAccessibilityNodeInfosByText("رفض") +
                            rootNode.findAccessibilityNodeInfosByText("Decline")
        
        endCallButtons.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        
        rootNode.recycle()
    }
}
