package com.example.simonmcdonnell.songle

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), DownloadXMLTask.DownloadXMLListener {
    private val TAG = "LOG_TAG"
    private val url = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set on click listener for play button
        play_button.setOnClickListener { _ ->
            val haveConnection = checkConnection()
            if (haveConnection) {
                DownloadXMLTask(this).execute(url)
            } else {
                displayToast("No connection")
            }
        }
    }

    fun checkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
                || networkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }

    fun displayToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun downloadComplete(songList: List<MyParser.Song>) {
        // Pick a random song from the list
        val rand = Random()
        val index = rand.nextInt(songList.size)
        val song = songList[index]
        // Display song and send it to MapsActivity
        displayToast(song.title)
        val mapsIntent = Intent(this, MapsActivity::class.java)
        mapsIntent.putExtra("MAP_ID", song.number)
        startActivity(mapsIntent)
    }
}
