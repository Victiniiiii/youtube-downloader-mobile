package com.Victiniiiii.ytdownloader

import com.chaquo.python.Python

object PythonBridge {
    private val py = Python.getInstance()
    private val downloader = py.getModule("downloader")

    fun download(url: String, downloadAudio: Boolean) {
        downloader.callAttr("download_video", url, downloadAudio)
    }

    fun getSupportedSites(): String {
        return downloader.callAttr("get_supported_sites").toString()
    }
}
