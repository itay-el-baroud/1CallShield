package com.callshield.service

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CallShieldInCallService : InCallService() {
    
    companion object {
        private const val TAG = "CallShieldInCallService"
        
        private val _currentCall = MutableStateFlow<Call?>(null)
        val currentCall: StateFlow<Call?> = _currentCall
        
        private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
        val callState: StateFlow<Int> = _callState
        
        private val _isRecording = MutableStateFlow(false)
        val isRecording: StateFlow<Boolean> = _isRecording
        
        private val _isMuted = MutableStateFlow(false)
        val isMuted: StateFlow<Boolean> = _isMuted
        
        private val _isOnHold = MutableStateFlow(false)
        val isOnHold: StateFlow<Boolean> = _isOnHold
        
        private val _isSpeakerOn = MutableStateFlow(false)
        val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "Call added: ${call.details.handle}")
        _currentCall.value = call
        _callState.value = call.state
        
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                _callState.value = state
                Log.d(TAG, "Call state changed: $state")
            }
            
            override fun onCallDestroyed(call: Call) {
                _currentCall.value = null
                _callState.value = Call.STATE_DISCONNECTED
                resetStates()
            }
        })
    }
    
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "Call removed")
        if (_currentCall.value == call) {
            _currentCall.value = null
            _callState.value = Call.STATE_DISCONNECTED
            resetStates()
        }
    }
    
    private fun resetStates() {
        _isRecording.value = false
        _isMuted.value = false
        _isOnHold.value = false
        _isSpeakerOn.value = false
    }
    
    // Actions
    fun answerCall() {
        currentCall.value?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }
    
    fun rejectCall() {
        currentCall.value?.disconnect()
    }
    
    fun endCall() {
        currentCall.value?.disconnect()
    }
    
    fun toggleMute() {
        val newState = !_isMuted.value
        _isMuted.value = newState
        // Mute logic via AudioManager
    }
    
    fun toggleHold() {
        currentCall.value?.let { call ->
            if (_isOnHold.value) {
                call.unhold()
            } else {
                call.hold()
            }
            _isOnHold.value = !_isOnHold.value
        }
    }
    
    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
        // Speaker logic via AudioManager
    }
    
    fun startRecording() {
        _isRecording.value = true
        // Recording implementation
    }
    
    fun stopRecording() {
        _isRecording.value = false
        // Stop recording
    }
}
