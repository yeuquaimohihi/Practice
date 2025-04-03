package com.tlu.demgio_th

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var seconds = 0
    private var isRunning = true
    private var backgroundThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeTextView = findViewById(R.id.timeTextView)

        startBackgroundThread()
    }

    private fun startBackgroundThread() {
        backgroundThread = Thread {
            try {
                while (isRunning) {
                    // Update the UI using the handler
                    handler.post {
                        timeTextView.text = "$seconds seconds"
                    }

                    // Increment seconds
                    seconds++

                    // Sleep for 1 second
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Thread interrupted
            }
        }
        backgroundThread?.start()
    }

    override fun onDestroy() {
        isRunning = false
        backgroundThread?.interrupt()
        super.onDestroy()
    }
}