package com.example.simonmcdonnell.songle

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.android.synthetic.main.activity_startup.*

class StartupActivity : AppCompatActivity() {
    private val songsUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml"
    private var receiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        // Register Broadcast Receiver
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(receiver, filter)
    }

    inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, p1: Intent?) {
            // Return a boolean value indicating whether we have internet connection
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            if (networkInfo?.type == ConnectivityManager.TYPE_WIFI
                    || networkInfo?.type == ConnectivityManager.TYPE_MOBILE) {
                DownloadXMLTask(this@StartupActivity).execute(songsUrl)
            } else {
                displayMessage("No Connection")
            }
        }
    }

    fun displayMessage(message: String) {
        val snackBar = Snackbar.make(startup_layout, message, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    class DownloadXMLTask(val caller: Activity): AsyncTask<String, Void, String>() {
        private val TAG = "LOG_TAG"

        override fun doInBackground(vararg urls: String): String {
            Log.v(TAG, "doInBackground")
            return try {
                Thread.sleep(1000)
                loadXmlFromNetwork(urls[0])
            } catch (e: IOException) {
                "Unable to load content, check internet connection"
            } catch (e: XmlPullParserException) {
                "Error parsing XML"
            }
        }

        private fun loadXmlFromNetwork(urlString: String): String {
            val stream = downloadUrl(urlString)
            val xmlString = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            return xmlString
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
            val mainIntent = Intent(caller, MainActivity::class.java)
            mainIntent.putExtra("XML", result)
            caller.startActivity(mainIntent)
            caller.overridePendingTransition(R.anim.fab_scale_up, R.anim.abc_fade_out)
            caller.finish()
        }
    }
}
