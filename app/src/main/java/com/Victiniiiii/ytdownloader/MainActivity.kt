package com.Victiniiiii.ytdownloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadBtn: Button
    private lateinit var audioSwitch: SwitchMaterial
    private lateinit var statusText: TextView
    private lateinit var versionText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing Python: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        val py = Python.getInstance()
        val downloader = py.getModule("downloader")

        urlInput = findViewById(R.id.urlInput)
        downloadBtn = findViewById(R.id.downloadBtn)
        audioSwitch = findViewById(R.id.audioSwitch)
        statusText = findViewById(R.id.statusText)
        versionText = findViewById(R.id.versionText)
        progressBar = findViewById(R.id.progressBar)

        Thread {
            try {
                val version = downloader.callAttr("get_ytdlp_version").toString()
                runOnUiThread {
                    versionText.text = "yt-dlp version: $version"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    versionText.text = "yt-dlp version: Unknown"
                }
            }
        }.start()

        downloadBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val downloadAudio = audioSwitch.isChecked
            
            progressBar.visibility = View.VISIBLE
            downloadBtn.isEnabled = false
            statusText.text = if (downloadAudio) "Downloading audio..." else "Downloading video..."
            statusText.visibility = View.VISIBLE

            Thread {
                try {
                    PythonBridge.download(url, downloadAudio)
                    val result = "Download complete!"
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        downloadBtn.isEnabled = true
                        statusText.text = result
                        
                        Toast.makeText(this, "Download complete!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        downloadBtn.isEnabled = true
                        statusText.text = "Error: ${e.message}"
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
}