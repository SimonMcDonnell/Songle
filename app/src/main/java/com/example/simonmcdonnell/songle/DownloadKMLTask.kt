package com.example.simonmcdonnell.songle

import android.os.AsyncTask
import android.util.Log
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class DownloadKMLTask(private val caller: DownloadKMLListener, private val lyrics: String, val song: MyParser.Song):
        AsyncTask<String, Void, String>() {
    private val TAG = "LOG_TAG"

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
        Log.v(TAG, "downloaded kml")
        return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        Log.v(TAG, "URL = $url")
        val conn = url.openConnection() as HttpURLConnection
        Log.v(TAG, "connection = $conn")
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
        caller.downloadComplete(result, lyrics, song)
    }
}