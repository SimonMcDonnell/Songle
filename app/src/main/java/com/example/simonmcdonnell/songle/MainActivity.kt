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
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.StandardCharsets
import java.util.*

class MainActivity : AppCompatActivity(), DownloadKMLTask.DownloadKMLListener, DownloadTXTTask.DownloadTXTListener {
    private val TAG = "LOG_TAG"
    private val contentUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"
    private lateinit var settings: SharedPreferences
    private lateinit var songList: List<MyParser.Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get Shared Preferences
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        // Get XML String and parse to get list of songs
        val extras = intent.extras
        val xmlString = extras["XML"] as String
        val xmlInputStream = xmlString.byteInputStream(StandardCharsets.UTF_8)
        val mParser = MyParser()
        songList = mParser.parse(xmlInputStream)
        // Set on click listener for play button
        play_button.setOnClickListener { _ ->
            val haveConnection = checkConnection()
            if (haveConnection) {
                playRandomSong()
            } else {
                displayMessage("No Connection")
            }
        }
        settings_button.setOnClickListener { _ ->
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }

    fun displayMessage(message: String) {
        val snackBar = Snackbar.make(constraint_layout, message, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    fun checkConnection(): Boolean {
        // Return a boolean value indicating whether we have internet connection
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
                || networkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }

    fun playRandomSong() {
        // Pick a random song from the list
        val rand = Random()
        val index = rand.nextInt(songList.size)
        val song = songList[index]
        // Display song and send it to MapsActivity
        DownloadTXTTask(this, song).execute(contentUrl + "${song.number}/lyrics.txt")
    }

    // Download complete for retrieving Lyrics
    override fun downloadComplete(lyrics: String, song: MyParser.Song) {
        displayMessage(song.title)
        // Get the difficulty level and select appropriate kml to download
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
