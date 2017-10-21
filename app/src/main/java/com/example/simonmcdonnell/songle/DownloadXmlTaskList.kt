package com.example.simonmcdonnell.songle

import android.os.AsyncTask
import android.util.Log
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadXmlTaskList(var songList: ArrayList<MyParser.Song>, var adapter: SongListAdapter):
        AsyncTask<String, Void, String>() {
    private val TAG = "LOG_TAG"
    private lateinit var songs: ArrayList<MyParser.Song>
    private val DOWNLOAD_SUCCESSFUL = "1"

    override fun doInBackground(vararg urls: String) : String {
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
        val stream = downloadUrl(urlString)
        // parse the input stream
        val mParser = MyParser()
        val songs = mParser.parse(stream)
        this.songs = songs
        return DOWNLOAD_SUCCESSFUL
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
        if (result == DOWNLOAD_SUCCESSFUL) {
            songList.clear()
            songList.addAll(songs)
            adapter.notifyDataSetChanged()
        }
    }
}