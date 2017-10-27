package com.example.simonmcdonnell.songle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_song_list.*

class SongListActivity : AppCompatActivity() {
    private val TAG = "LOG_TAG"
    private lateinit var songList: ArrayList<MyParser.Song>
    private lateinit var adapter: SongListAdapter
    private var receiver  = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        // Register Broadcast receiver to track connection changes
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(receiver, filter)
        // Set up recyclerview
//        songList = ArrayList()
//        adapter = SongListAdapter(this, songList)
//        recyclerview.adapter = adapter
//        recyclerview.layoutManager = LinearLayoutManager(this)
    }

    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(TAG, "onReceive")
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            Log.v(TAG, networkInfo?.type.toString())
            Log.v(TAG, ConnectivityManager.TYPE_WIFI.toString())
            if (networkInfo?.type == ConnectivityManager.TYPE_WIFI
                    || networkInfo?.type == ConnectivityManager.TYPE_MOBILE) {
                // If there is connection we download the latest XML file
                DownloadXmlTaskList(songList, adapter)
                        .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            }
        }
    }

//    override fun onClick(songNumber: String) {
//        val toast = Toast.makeText(this, songNumber, Toast.LENGTH_SHORT)
//        toast.show()
//        val mapsIntent = Intent(this, MapsActivity::class.java)
//        startActivity(mapsIntent)
//    }
}
