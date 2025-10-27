package com.Victiniiiii.ytdownloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val downloader = py.getModule("downloader")

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val downloadBtn = findViewById<Button>(R.id.downloadBtn)

        downloadBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread {
                try {
                    downloader.callAttr("download_video", url)
                    runOnUiThread {
                        Toast.makeText(this, "Download complete", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
}
