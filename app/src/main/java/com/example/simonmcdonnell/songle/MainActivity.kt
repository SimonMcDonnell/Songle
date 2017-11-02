package com.example.simonmcdonnell.songle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.Toast
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity(), DownloadXMLTask.DownloadXMLListener, DownloadKMLTask.DownloadKMLListener,
        DownloadTXTTask.DownloadTXTListener {
    private val TAG = "LOG_TAG"
    private val songsUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml"
    private val contentUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"
    private lateinit var settings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get Shared Preferences
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        // Set on click listener for play button
        play_button.setOnClickListener { _ ->
            val haveConnection = checkConnection()
            if (haveConnection) {
                DownloadXMLTask(this).execute(songsUrl)
            } else {
                displayMessage("No connection")
            }
        }
        settings_button.setOnClickListener { _ ->
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }

    fun checkConnection(): Boolean {
        // Return a boolean value indicating whether we have internet connection
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
                || networkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }

    fun displayMessage(message: String) {
        val snackbar = Snackbar.make(constraint_layout, message, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    override fun downloadComplete(songList: List<MyParser.Song>) {
        // Pick a random song from the list
        val rand = Random()
        val index = rand.nextInt(songList.size)
        val song = songList[index]
        // Display song and send it to MapsActivity
        DownloadTXTTask(this, song).execute(contentUrl + "${song.number}/lyrics.txt")
    }

    override fun downloadComplete(lyrics: String, song: MyParser.Song) {
        displayMessage(song.title)
        Log.v(TAG, "Here is the lyrics $lyrics")
        val difficulty = settings.getString("difficulty", "2").toInt()
        DownloadKMLTask(this, lyrics, song).execute(contentUrl + "${song.number}/map$difficulty.kml")
    }

    override fun downloadComplete(kmlString: String, lyrics: String, song: MyParser.Song) {
        val mapsIntent = Intent(this, MapsActivity::class.java)
        mapsIntent.putExtra("ID", song.number)
        mapsIntent.putExtra("NAME", song.title)
        mapsIntent.putExtra("ARTIST", song.artist)
        mapsIntent.putExtra("LINK", song.link)
        mapsIntent.putExtra("LYRICS", lyrics)
        mapsIntent.putExtra("KML", kmlString)
        startActivity(mapsIntent)
    }
}
