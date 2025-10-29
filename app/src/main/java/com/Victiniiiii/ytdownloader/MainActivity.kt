package com.Victiniiiii.ytdownloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private var downloadFolder: String = "/sdcard/Download"
    private lateinit var urlInput: EditText
    private lateinit var downloadBtn: Button
    private lateinit var modeToggleGroup: MaterialButtonToggleGroup
    private lateinit var versionText: TextView
    private lateinit var progressBar: ProgressBar

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
            versionText = findViewById(R.id.versionText)
            progressBar = findViewById(R.id.progressBar)

            modeToggleGroup.check(R.id.audioModeButton)

            modeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.audioModeButton -> {
                            findViewById<Button>(R.id.audioModeButton).setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.holo_red_light)
                            )
                            findViewById<Button>(R.id.videoModeButton).setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.darker_gray)
                            )
                        }
                        R.id.videoModeButton -> {
                            findViewById<Button>(R.id.videoModeButton).setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.holo_red_light)
                            )
                            findViewById<Button>(R.id.audioModeButton).setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.darker_gray)
                            )
                        }
                    }
                }
            }

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

            val selectFolderBtn: Button = findViewById(R.id.selectFolderBtn)
            selectFolderBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                folderPickerLauncher.launch(intent)
            }

            downloadBtn.setOnClickListener {
                val url = urlInput.text.toString().trim()
                if (url.isEmpty()) {
                    Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val downloadAudio = modeToggleGroup.checkedButtonId == R.id.audioModeButton

                progressBar.visibility = View.VISIBLE
                downloadBtn.isEnabled = false
                downloadBtn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

                Thread {
                    try {
                        PythonBridge.download(url, downloadAudio, downloadFolder)
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            downloadBtn.isEnabled = true
                            downloadBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
                            Toast.makeText(this, "Download complete!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            downloadBtn.isEnabled = true
                            downloadBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            logErrorToFile(e)
            Toast.makeText(this, "App crashed. Check log file in /sdcard/Download.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private val folderPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val path = uri.path ?: return@registerForActivityResult
                downloadFolder = path.replace("/tree/primary:", "/sdcard/")
                Toast.makeText(this, "Folder selected: $downloadFolder", Toast.LENGTH_SHORT).show()
            }
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