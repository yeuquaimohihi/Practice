package com.example.telephony_th

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Date

class PhoneCallReceiver : BroadcastReceiver() {
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var incomingNumber: String? = null
    private var callStartTime: Date? = null
    private var isIncoming = false
    private var savedNumber: String? = null
    private var wasRinging = false // Add this flag

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
        val number = intent.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d("PhoneCallReceiver", "Action: ${intent.action}, State: $stateStr, Number: $number")

        var state = TelephonyManager.CALL_STATE_IDLE
        when (stateStr) {
            TelephonyManager.EXTRA_STATE_IDLE -> state = TelephonyManager.CALL_STATE_IDLE
            TelephonyManager.EXTRA_STATE_OFFHOOK -> state = TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_RINGING -> state = TelephonyManager.CALL_STATE_RINGING
        }

        onCallStateChanged(context, state, number)
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        Log.d("PhoneCallReceiver", "State changed from $lastState to $state, number: $number")

        // Save any non-null number we receive
        if (number != null) {
            incomingNumber = number
            Log.d("PhoneCallReceiver", "Saving incoming number: $incomingNumber")
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                wasRinging = true  // Set flag when call is ringing
                Log.d("PhoneCallReceiver", "Call is ringing, wasRinging set to true")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Answered call
                wasRinging = false  // Reset flag if answered
                Log.d("PhoneCallReceiver", "Call answered, wasRinging set to false")
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                if (wasRinging) {
                    // This was a missed call
                    Log.d("PhoneCallReceiver", "Missed call detected with wasRinging true, number: $incomingNumber")
                    if (incomingNumber != null) {
                        sendSMS(context, incomingNumber!!)
                    } else {
                        Log.e("PhoneCallReceiver", "Cannot send SMS: missing incoming number")
                    }
                    wasRinging = false  // Reset flag
                }
            }
        }

        lastState = state
    }

    private fun sendSMS(context: Context, phoneNumber: String) {
        try {
            val prefs = context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE)
            val message = prefs.getString("autoReplyMessage",
                "Xin lỗi, tôi đang bận. Tôi sẽ gọi lại cho bạn sau.")

            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            Log.d("PhoneCallReceiver", "Attempting to send SMS to $phoneNumber with message: $message")
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("PhoneCallReceiver", "SMS sent successfully to $phoneNumber")
        } catch (e: Exception) {
            Log.e("PhoneCallReceiver", "Failed to send SMS: ${e.message}", e)
        }
    }
}