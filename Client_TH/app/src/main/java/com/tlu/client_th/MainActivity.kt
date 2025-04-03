package com.tlu.client_th

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var etIpAddress: EditText
    private lateinit var etPort: EditText
    private lateinit var btnConnect: Button
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvLog: TextView

    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        etIpAddress = findViewById(R.id.etIpAddress)
        etPort = findViewById(R.id.etPort)
        btnConnect = findViewById(R.id.btnConnect)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvLog = findViewById(R.id.tvLog)

        // Set click listeners
        btnConnect.setOnClickListener {
            if (!isConnected) {
                connectToServer()
            } else {
                disconnectFromServer()
            }
        }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty() && isConnected) {
                sendMessage(message)
                etMessage.text.clear()
            }
        }
    }

    private fun connectToServer() {
        val ipAddress = etIpAddress.text.toString().trim()
        val portStr = etPort.text.toString().trim()

        if (ipAddress.isEmpty() || portStr.isEmpty()) {
            showToast("IP address and port are required")
            return
        }

        val port = portStr.toIntOrNull()
        if (port == null || port <= 0 || port > 65535) {
            showToast("Invalid port number")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create socket
                clientSocket = Socket(ipAddress, port)
                writer = PrintWriter(BufferedWriter(
                    OutputStreamWriter(clientSocket!!.getOutputStream())), true)
                reader = BufferedReader(
                    InputStreamReader(clientSocket!!.getInputStream()))

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    isConnected = true
                    btnConnect.text = "Disconnect"
                    btnSend.isEnabled = true
                    appendToLog("Connected to server: $ipAddress:$port")

                    // Start listening for incoming messages
                    startMessageListener()
                }
            } catch (e: Exception) {
                Log.e("TCP", "Error connecting: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast("Failed to connect: ${e.message}")
                    appendToLog("Connection failed: ${e.message}")
                }
            }
        }
    }

    private fun disconnectFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer?.close()
                reader?.close()
                clientSocket?.close()

                withContext(Dispatchers.Main) {
                    isConnected = false
                    btnConnect.text = "Connect to Server"
                    btnSend.isEnabled = false
                    appendToLog("Disconnected from server")
                }
            } catch (e: Exception) {
                Log.e("TCP", "Error disconnecting: ${e.message}")
            }
        }
    }

    private fun sendMessage(message: String) {
        if (!isConnected || writer == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer?.println(message)
                withContext(Dispatchers.Main) {
                    appendToLog("Sent: $message")
                }
            } catch (e: Exception) {
                Log.e("TCP", "Error sending: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast("Failed to send message")
                    appendToLog("Send failed: ${e.message}")
                }
            }
        }
    }

    private fun startMessageListener() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isConnected) {
                    val message = reader?.readLine()
                    if (message != null) {
                        withContext(Dispatchers.Main) {
                            appendToLog("Received: $message")
                        }
                    } else {
                        // Connection closed
                        withContext(Dispatchers.Main) {
                            disconnectFromServer()
                        }
                        break
                    }
                }
            } catch (e: IOException) {
                if (isConnected) {
                    withContext(Dispatchers.Main) {
                        showToast("Connection lost")
                        disconnectFromServer()
                    }
                }
            }
        }
    }

    private fun appendToLog(message: String) {
        val currentLog = tvLog.text.toString()
        tvLog.text = if (currentLog.isEmpty()) message else "$currentLog\n$message"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromServer()
    }
}