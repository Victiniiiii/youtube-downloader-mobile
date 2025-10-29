package com.Victiniiiii.ytdownloader

import com.chaquo.python.Python

object PythonBridge {
    private val py = Python.getInstance()
    private val downloader = py.getModule("downloader")

    fun download(url: String, downloadAudio: Boolean, folder: String) {
        downloader.callAttr("download_video", url, downloadAudio, folder)
    }
}
