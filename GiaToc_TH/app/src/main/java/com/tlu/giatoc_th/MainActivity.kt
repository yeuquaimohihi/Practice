package com.tlu.giatoc_th

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var xValueTextView: TextView
    private lateinit var yValueTextView: TextView
    private lateinit var zValueTextView: TextView
    private lateinit var ballImage: ImageView

    // Ball position
    private var xPos = 0f
    private var yPos = 0f

    // Dampening factor for smoother movement
    private val DAMPENING = 0.1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextViews
        xValueTextView = findViewById(R.id.x_value)
        yValueTextView = findViewById(R.id.y_value)
        zValueTextView = findViewById(R.id.z_value)

        // Initialize ImageView
        ballImage = findViewById(R.id.ball_image)

        // Get initial position of the ball (center of the screen)
        ballImage.post {
            xPos = (ballImage.parent as android.view.ViewGroup).width / 2f - ballImage.width / 2f
            yPos = (ballImage.parent as android.view.ViewGroup).height / 2f - ballImage.height / 2f
            updateBallPosition()
        }

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Get acceleration values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Display acceleration values
            xValueTextView.text = "X: ${"%.2f".format(x)}"
            yValueTextView.text = "Y: ${"%.2f".format(y)}"
            zValueTextView.text = "Z: ${"%.2f".format(z)}"

            // Update ball position using acceleration
            // Invert x since we want the ball to move in the direction of tilt
            // y is positive in downward direction in Android
            xPos -= x * DAMPENING
            yPos += y * DAMPENING

            // Keep the ball within the screen boundaries
            val parent = ballImage.parent as android.view.ViewGroup
            val maxX = parent.width - ballImage.width
            val maxY = parent.height - ballImage.height

            if (xPos < 0) xPos = 0f
            if (xPos > maxX) xPos = maxX.toFloat()
            if (yPos < 0) yPos = 0f
            if (yPos > maxY) yPos = maxY.toFloat()

            updateBallPosition()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed for this demo, but required by SensorEventListener interface
    }

    private fun updateBallPosition() {
        ballImage.translationX = xPos
        ballImage.translationY = yPos
    }
}