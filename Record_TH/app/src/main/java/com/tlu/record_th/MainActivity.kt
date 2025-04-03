package com.tlu.record_th

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 200
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var recordingUri: Uri? = null
    private lateinit var btnRecord: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var lvRecordings: ListView
    private val recordings = ArrayList<Recording>()
    private lateinit var recordingsAdapter: ArrayAdapter<Recording>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRecord = findViewById(R.id.btnRecord)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvRecordingStatus)
        lvRecordings = findViewById(R.id.lvRecordings)

        // Setup recording list adapter
        recordingsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            recordings
        )
        lvRecordings.adapter = recordingsAdapter

        // Check and request permissions
        if (!checkPermissions()) {
            requestPermissions()
        }

        btnRecord.setOnClickListener {
            if (checkPermissions()) {
                startRecording()
            } else {
                requestPermissions()
            }
        }

        btnStop.setOnClickListener {
            stopRecording()
        }

        // Set up click listener for recordings
        lvRecordings.setOnItemClickListener { _, _, position, _ ->
            val recording = recordings[position]
            playRecording(recording.uri)
        }

        // Load existing recordings
        loadRecordings()
    }

    private fun checkPermissions(): Boolean {
        val recordPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return recordPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                loadRecordings()
            } else {
                Toast.makeText(
                    this,
                    "Permissions denied - the app needs these permissions to work",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startRecording() {
        // Release any previous recorder
        releaseRecorder()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val displayName = "recording_$timestamp.mp3"

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        recordingUri = contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        recordingUri?.let { uri ->
            try {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor)
                    prepare()
                    start()
                }

                isRecording = true
                btnRecord.isEnabled = false
                btnStop.isEnabled = true
                tvStatus.text = "Recording in progress..."

            } catch (e: IOException) {
                Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                contentResolver.delete(uri, null, null)
                releaseRecorder()
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null

                recordingUri?.let { uri ->
                    // Update IS_PENDING to 0 to make it visible
                    val values = ContentValues().apply {
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    contentResolver.update(uri, values, null, null)

                    // Add to our list
                    val cursor = contentResolver.query(
                        uri,
                        arrayOf(
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DATE_ADDED
                        ),
                        null,
                        null,
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName = it.getString(0)
                            val dateAdded = it.getLong(1)
                            val recording = Recording(displayName, uri, dateAdded * 1000)
                            recordings.add(0, recording)
                            recordingsAdapter.notifyDataSetChanged()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error stopping recording: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                isRecording = false
                btnRecord.isEnabled = true
                btnStop.isEnabled = false
                tvStatus.text = "Recording stopped"
            }
        }
    }

    private fun loadRecordings() {
        recordings.clear()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Music/Recordings%")
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)

                val contentUri: Uri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                val recording = Recording(name, contentUri, dateAdded * 1000)
                recordings.add(recording)
            }
        }

        recordingsAdapter.notifyDataSetChanged()
    }

    private fun playRecording(uri: Uri) {
        // Stop any currently playing audio
        releasePlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                prepare()
                start()
            }

            mediaPlayer?.setOnCompletionListener {
                releasePlayer()
                tvStatus.text = "Playback completed"
            }

            tvStatus.text = "Playing recording"

        } catch (e: Exception) {
            Toast.makeText(this, "Error playing recording: ${e.message}", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Playback failed"
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore exceptions during cleanup
        }
        mediaRecorder = null
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore exceptions during cleanup
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRecorder()
        releasePlayer()
    }

    data class Recording(val name: String, val uri: Uri, val dateAdded: Long) {
        override fun toString(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateString = sdf.format(Date(dateAdded))
            return "$name ($dateString)"
        }
    }
}