package com.tlu.blockphone_th

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import android.telecom.TelecomManager
import android.os.Build
import android.annotation.SuppressLint

class CallReceiver : BroadcastReceiver() {
    private val TAG = "CallReceiver"

    // List of blocked phone numbers
    private val blockedNumbers = listOf(
        "0123456789",
        "0987654321",
        // Add more blocked numbers here
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            // Check if the phone state is ringing (incoming call)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d(TAG, "Incoming call from: $incomingNumber")

                // Check if the number is in the blocked list
                if (incomingNumber != null && isNumberBlocked(incomingNumber)) {
                    Log.d(TAG, "Number is blocked. Attempting to end call.")
                    endCall(context)
                }
            }
        }
    }

    private fun isNumberBlocked(phoneNumber: String): Boolean {
        return blockedNumbers.contains(phoneNumber) ||
               blockedNumbers.any { phoneNumber.endsWith(it) }
    }

    @SuppressLint("MissingPermission")
    private fun endCall(context: Context) {
        try {
            // Method for Android 9+ (requires ANSWER_PHONE_CALLS permission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
                Log.d(TAG, "Call ended using Telecom API")
            } else {
                // For older versions - this might not work on all devices
                // May need reflection or device-specific methods
                Log.d(TAG, "Call ending not implemented for this Android version")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end call: ${e.message}")
        }
    }
}