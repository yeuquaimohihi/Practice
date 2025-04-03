package com.example.telephony_th

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var saveMessageButton: Button
    private lateinit var permissionStatusTextView: TextView
    private lateinit var statusTextView: TextView

    private val sharedPrefs by lazy {
        getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE)
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermissionStatus(permissions.all { it.value })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById<EditText>(R.id.messageEditText)
        saveMessageButton = findViewById<Button>(R.id.saveMessageButton)
        permissionStatusTextView = findViewById<TextView>(R.id.permissionStatusTextView)
        statusTextView = findViewById<TextView>(R.id.statusTextView)

        // Load saved message if any
        val savedMessage = sharedPrefs.getString("autoReplyMessage",
            "Xin lỗi, tôi đang bận. Tôi sẽ gọi lại cho bạn sau.")
        messageEditText.setText(savedMessage)

        saveMessageButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotBlank()) {
                saveAutoReplyMessage(message)
                Toast.makeText(this, "Đã lưu tin nhắn trả lời", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Tin nhắn không thể trống", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionsIfNeeded()
    }

    private fun saveAutoReplyMessage(message: String) {
        sharedPrefs.edit().putString("autoReplyMessage", message).apply()
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            updatePermissionStatus(true)
        }
    }

    private fun updatePermissionStatus(allGranted: Boolean) {
        if (allGranted) {
            permissionStatusTextView.setText("Tất cả quyền đã được cấp")
            permissionStatusTextView.setTextColor(Color.GREEN)
            statusTextView.setText("Dịch vụ đang hoạt động")
        } else {
            permissionStatusTextView.setText("Chưa cấp đủ quyền cần thiết")
            permissionStatusTextView.setTextColor(Color.RED)
            statusTextView.setText("Dịch vụ không hoạt động")
        }
    }
}