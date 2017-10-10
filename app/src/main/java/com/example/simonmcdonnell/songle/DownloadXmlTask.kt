package com.example.simonmcdonnell.songle

import android.os.AsyncTask
import android.util.Log
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadXmlTask(private val caller: MainActivity): AsyncTask<String, Void, String>() {
    private val TAG = "LOG_TAG"

    // Callback to UI thread
    interface DownloadCompleteListener {
        fun downloadComplete(result: String)
    }

    override fun doInBackground(vararg urls: String): String {
        Log.v(TAG, "doInBackground")
        return try {
            loadXmlFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content, check internet connection"
        } catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadXmlFromNetwork(urlString: String): String {
        val result = StringBuilder()
        val stream = downloadUrl(urlString)
        // parse the input stream
        val mParser = MyParser()
        val entries = mParser.parse(stream)
        Log.v("LOGTAG", entries.size.toString())
        return result.toString()
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
        Log.v(TAG, result)
        caller.downloadComplete(result)
    }
}