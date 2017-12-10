package com.example.simonmcdonnell.songle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_start.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

class StartActivity : AppCompatActivity(), DownloadKMLTask.DownloadKMLListener, DownloadTXTTask.DownloadTXTListener {
    private val TAG = "LOG_TAG"
    private val contentUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"
    private val REQUEST_CODE = 101
    private val SUCCESS = 1
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var settings: SharedPreferences
    private lateinit var songList: List<MyParser.Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        // Get Shared Preferences
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        // Get XML String and parse to get list of songs
        val extras = intent.extras
        val xmlString = extras["XML"] as String
        val xmlInputStream = xmlString.byteInputStream(StandardCharsets.UTF_8)
        val mParser = MyParser()
        songList = mParser.parse(xmlInputStream)
        // Set on click listeners for buttons
        play_button.setOnClickListener { _ ->
            // Check if we still have connection for downloading KML
            val haveConnection = checkConnection()
            if (haveConnection) {
                playRandomSong()
            } else {
                displayMessage("No Internet Connection")
            }
        }
        settings_button.setOnClickListener { _ ->
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
        completed_button.setOnClickListener { _ ->
            val completedIntent = Intent(this, CompletedActivity::class.java)
            startActivity(completedIntent)
        }
    }

    private fun displayMessage(message: String) = Snackbar.make(start_layout, message, Snackbar.LENGTH_SHORT).show()

    private fun checkConnection(): Boolean {
        // Return a boolean value indicating whether we have internet connection
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
                || networkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }

    private fun playRandomSong() {
        // Can we access user's location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Pick a random song from the list
            val rand = Random()
            val index = rand.nextInt(songList.size)
            val song = songList[index]
            // Download lyrics and KML for the chosen song
            DownloadTXTTask(this, song).execute(contentUrl + "${song.number}/lyrics.txt")
        } else {
            // Ask for permission to use location
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    // Download complete for retrieving Lyrics
    override fun downloadComplete(lyrics: String, song: MyParser.Song) {
        displayMessage(song.title)
        // Get the difficulty level and select appropriate KML to download
        val difficulty = settings.getString("difficulty", "3").toInt()
        DownloadKMLTask(this, lyrics, song).execute(contentUrl + "${song.number}/map$difficulty.kml")
    }

    // Download complete for retrieving KML
    override fun downloadComplete(kmlString: String, lyrics: String, song: MyParser.Song) {
        // Lyrics and KML are passed to Maps Activity
        val mapsIntent = Intent(this, MapsActivity::class.java)
        mapsIntent.putExtra("ID", song.number)
        mapsIntent.putExtra("NAME", song.title)
        mapsIntent.putExtra("ARTIST", song.artist)
        mapsIntent.putExtra("LINK", song.link)
        mapsIntent.putExtra("LYRICS", lyrics)
        mapsIntent.putExtra("KML", kmlString)
        startActivityForResult(mapsIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // If the user guessed the song, add it to the list of completed songs and update XP
        if (requestCode == REQUEST_CODE && resultCode == SUCCESS) {
            // Retrieve completed song data
            val extras = data.extras
            val artist = extras.getString("ARTIST") as String
            val title = extras.getString("NAME") as String
            val link = extras.getString("LINK") as String
            val song = MyParser.Song("", artist, title, link)
            updateCompletedList(song)
            updateXP()
        }
    }

    private fun updateCompletedList(song: MyParser.Song) {
        // Retrieve completed list from SharedPreferences
        val gson = Gson()
        val jsonList = settings.getString("PLAYED", "")
        val editor = settings.edit()
        val type = object: TypeToken<ArrayList<MyParser.Song>>() {}.type
        var playedList: ArrayList<MyParser.Song>? = gson.fromJson<ArrayList<MyParser.Song>>(jsonList, type)
        if (playedList == null) playedList = ArrayList()
        // If song is already completed then ignore
        var seen = false
        for (s in playedList) {
            if (s.title == song.title) seen = true
        }
        // If song has not been completed before, add to the list and save
        if (!seen) playedList.add(song)
        val json = gson.toJson(playedList)
        editor.putString("PLAYED", json)
        editor.apply()
    }

    private fun updateXP() {
        val xp = settings.getInt("XP", 0)
        val difficulty = settings.getString("difficulty", "3").toInt()
        val editor = settings.edit()
        // Reward based on difficulty of map and if timed mode was enabled
        var reward = 0
        when (difficulty) {
            1 -> reward += 25
            2 -> reward += 20
            3 -> reward += 15
            4 -> reward += 10
            5 -> reward += 5
        }
        // If timed mode was on, add additional 10XP to reward
        val timed = settings.getBoolean("timer", false)
        if (timed) reward += 10
        // Update the player's XP points
        editor.putInt("XP", xp + reward)
        editor.apply()
    }
}
