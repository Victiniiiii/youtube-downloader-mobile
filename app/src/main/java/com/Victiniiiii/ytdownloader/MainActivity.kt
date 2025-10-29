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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadBtn: Button
    private lateinit var modeToggleGroup: MaterialButtonToggleGroup
    private lateinit var statusText: TextView
    private lateinit var versionText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var supportedSitesText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val downloader = py.getModule("downloader")

            urlInput = findViewById(R.id.urlInput)
            downloadBtn = findViewById(R.id.downloadBtn)
            modeToggleGroup = findViewById(R.id.modeToggleGroup)
            statusText = findViewById(R.id.statusText)
            versionText = findViewById(R.id.versionText)
            progressBar = findViewById(R.id.progressBar)
            supportedSitesText = findViewById(R.id.supportedSitesText)

            modeToggleGroup.check(R.id.audioModeButton)

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

                val downloadAudio = modeToggleGroup.checkedButtonId == R.id.audioModeButton

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

            Thread {
                try {
                    val supportedSites = PythonBridge.getSupportedSites()
                    runOnUiThread {
                        supportedSitesText.text = supportedSites
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        supportedSitesText.text = "Error loading supported sites."
                    }
                }
            }.start()
        } catch (e: Exception) {
            logErrorToFile(e)
            Toast.makeText(this, "App crashed. Check log file in /sdcard/Download.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun logErrorToFile(e: Exception) {
        try {
            val logFile = File("/sdcard/Download/app_crash_log.txt")
            val writer = PrintWriter(FileWriter(logFile, true))
            writer.println("Crash Log: ${System.currentTimeMillis()}")
            writer.println(e.message)
            e.printStackTrace(writer)
            writer.println()
            writer.close()
        } catch (fileException: Exception) {
        }
    }
}