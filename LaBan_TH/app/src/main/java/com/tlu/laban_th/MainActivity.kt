package com.tlu.laban_th

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private lateinit var compassImage: ImageView
    private lateinit var degreeTextView: TextView
    private lateinit var directionTextView: TextView

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var currentDegree = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassImage = findViewById(R.id.compassImage)
        degreeTextView = findViewById(R.id.degreeTextView)
        directionTextView = findViewById(R.id.directionTextView)

        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get and check accelerometer sensor
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelSensor != null) {
            accelerometer = accelSensor
        } else {
            degreeTextView.text = "Accelerometer not available"
            return
        }

        // Get and check magnetometer sensor
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magnetSensor != null) {
            magnetometer = magnetSensor
        } else {
            degreeTextView.text = "Magnetometer not available"
            return
        }
    }

    override fun onResume() {
        super.onResume()

        // Register sensor listeners
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()

        // Unregister sensor listeners to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Update readings based on sensor type
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
        }

        // Calculate orientation using rotation matrix
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    private fun updateOrientationAngles() {
        // Update rotation matrix
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // Get orientation angles from rotation matrix
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert radians to degrees
        val degree = (Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360

        // Update UI with direction and angle
        updateCompassUI(degree.toFloat())
    }

    private fun updateCompassUI(degree: Float) {
        // Set the text to display the degree
        val degreeFormatted = String.format("%.1f°", degree)
        degreeTextView.text = degreeFormatted

        // Determine direction based on degree
        val direction = getDirectionFromDegree(degree)
        directionTextView.text = direction

        // Create rotation animation
        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        // Set animation properties
        rotateAnimation.duration = 250
        rotateAnimation.fillAfter = true

        // Apply animation to compass image
        compassImage.startAnimation(rotateAnimation)

        // Save current degree for next animation
        currentDegree = -degree
    }

    private fun getDirectionFromDegree(degree: Float): String {
        return when {
            degree >= 337.5 || degree < 22.5 -> "North"
            degree >= 22.5 && degree < 67.5 -> "Northeast"
            degree >= 67.5 && degree < 112.5 -> "East"
            degree >= 112.5 && degree < 157.5 -> "Southeast"
            degree >= 157.5 && degree < 202.5 -> "South"
            degree >= 202.5 && degree < 247.5 -> "Southwest"
            degree >= 247.5 && degree < 292.5 -> "West"
            else -> "Northwest"
        }
    }
}