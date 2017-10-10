package com.example.simonmcdonnell.songle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DownloadXmlTask.DownloadCompleteListener {
    private val TAG = "LOG_TAG"
    private var receiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Register Broadcast Receiver to track connection changes
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(receiver, filter)
        play_button.setOnClickListener { View ->
            val mapsIntent = Intent(this, MapsActivity::class.java)
            startActivity(mapsIntent)
        }
    }

    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(TAG, "onReceive")
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            if (networkInfo?.type == ConnectivityManager.TYPE_WIFI
                    || networkInfo?.type == ConnectivityManager.TYPE_MOBILE) {
                // If there is connection we download the latest XML file
                DownloadXmlTask(MainActivity()).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/")
            }
        }
    }

    // Function called when download of XML is complete
    override fun downloadComplete(result: String) {
        Log.v(TAG, "downloadComplete")
//        val toast = Toast.makeText(this, "Callback called!", Toast.LENGTH_SHORT)
//        toast.show()
    }
}
