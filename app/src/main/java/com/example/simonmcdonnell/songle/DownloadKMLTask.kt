package com.example.simonmcdonnell.songle

import android.os.AsyncTask
import android.util.Log
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class DownloadKMLTask(val caller: DownloadKMLListener, val lyrics: String, val song: MyParser.Song):
        AsyncTask<String, Void, String>() {
    private val TAG = "LOG_TAG"
    private val DOWNLOAD_SUCCESSFUL = "1"
    private lateinit var byteArr: ByteArray

    interface DownloadKMLListener {
        fun downloadComplete(kmlString: String, lyrics: String, song: MyParser.Song)
    }

    override fun doInBackground(vararg urls: String): String {
        Log.v(TAG, "doInBackground")
        return try {
            loadKMLFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content, check internet connection"
        } catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadKMLFromNetwork(urlString: String): String {
        val stream = downloadUrl(urlString)
        val kmlString = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        return kmlString
//        val out = ByteArrayOutputStream(8192)
//        val buffer = ByteArray(8192)
//        while (true) {
//            val length = stream.read(buffer)
//            if (length <= 0)
//                break
//            out.write(buffer, 0, length)
//        }
//        out.flush()
//        byteArr = out.toByteArray()
//        return DOWNLOAD_SUCCESSFUL
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        // Starts the query
        conn.connect()
        return conn.inputStream
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
//        if (result == DOWNLOAD_SUCCESSFUL) {
//            caller.downloadComplete(byteArr)
//        }
        caller.downloadComplete(result, lyrics, song)
    }
}