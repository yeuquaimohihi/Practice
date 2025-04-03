package com.tlu.playvideosp_th

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController
    private lateinit var btnPickVideo: Button
    private lateinit var btnPlayUrl: Button
    private lateinit var etVideoUrl: EditText

    private val READ_EXTERNAL_STORAGE_REQUEST = 101
    private val PICK_VIDEO_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        videoView = findViewById(R.id.videoView)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        btnPlayUrl = findViewById(R.id.btnPlayUrl)
        etVideoUrl = findViewById(R.id.etVideoUrl)

        // Set up MediaController
        mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Set up click listeners
        btnPickVideo.setOnClickListener {
            checkPermissionAndPickVideo()
        }

        btnPlayUrl.setOnClickListener {
            val videoUrl = etVideoUrl.text.toString().trim()
            if (videoUrl.isNotEmpty()) {
                playVideoFromUrl(videoUrl)
            } else {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle video completion
        videoView.setOnCompletionListener {
            Toast.makeText(this, "Video playback completed", Toast.LENGTH_SHORT).show()
        }

        // Handle video errors
        videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(
                this,
                "Error occurred: what=$what, extra=$extra",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }

    private fun checkPermissionAndPickVideo() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs permission to access your videos")
                    .setPositiveButton("OK") { _, _ ->
                        requestStoragePermission()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            } else {
                requestStoragePermission()
            }
        } else {
            pickVideoFromGallery()
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_REQUEST
        )
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }

    private fun playVideoFromUrl(url: String) {
        try {
            videoView.setVideoURI(Uri.parse(url))
            videoView.requestFocus()
            videoView.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing video: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,  // Changed from Array<out String>
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    pickVideoFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedVideoUri = data?.data
            if (selectedVideoUri != null) {
                try {
                    videoView.setVideoURI(selectedVideoUri)
                    videoView.requestFocus()
                    videoView.start()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error playing video: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause video when activity is paused
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources
        videoView.stopPlayback()
    }
}