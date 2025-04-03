package com.tlu.downloadimage_th

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    private lateinit var urlEditText: EditText
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        urlEditText = findViewById(R.id.urlEditText)
        downloadButton = findViewById(R.id.downloadButton)
        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)

        // Set up button click listener
        downloadButton.setOnClickListener {
            val imageUrl = urlEditText.text.toString().trim()
            if (imageUrl.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Start the download task
            ImageDownloadTask().execute(imageUrl)
        }
    }

    // AsyncTask to download image in background
    @Suppress("DEPRECATION") // AsyncTask is deprecated but still works for this example
    private inner class ImageDownloadTask : AsyncTask<String, Int, Bitmap?>() {
        override fun onPreExecute() {
            // Show progress before starting
            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String): Bitmap? {
            val imageUrl = params[0]
            var bitmap: Bitmap? = null
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                // Get the content length to track progress
                val contentLength = connection.contentLength
                val inputStream: InputStream = connection.inputStream

                // Create a buffer to read the data
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0

                // Read the data in chunks and report progress
                val data = ArrayList<Byte>()
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    for (i in 0 until bytesRead) {
                        data.add(buffer[i])
                    }

                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength)
                        publishProgress(progress)
                    }
                }

                // Convert the byte array to bitmap
                val byteArray = ByteArray(data.size)
                for (i in data.indices) {
                    byteArray[i] = data[i]
                }

                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        override fun onProgressUpdate(vararg values: Int?) {
            // Update the progress bar
            progressBar.progress = values[0] ?: 0
        }

        override fun onPostExecute(result: Bitmap?) {
            // Hide progress and show image if download was successful
            progressBar.visibility = View.GONE

            if (result != null) {
                imageView.setImageBitmap(result)
                Toast.makeText(this@MainActivity, "Download complete", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to download image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}