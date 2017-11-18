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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), DownloadKMLTask.DownloadKMLListener, DownloadTXTTask.DownloadTXTListener {
    private val TAG = "LOG_TAG"
    private val contentUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"
    private val REQUEST_CODE = 101
    private val SUCCESS = 1
    private val FAILURE = 0
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
        completed_button.setOnClickListener { _ ->
            val completedIntent = Intent(this, CompletedActivity::class.java)
            startActivity(completedIntent)
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
        // Download lyrics for the chosen song
        DownloadTXTTask(this, song).execute(contentUrl + "${song.number}/lyrics.txt")
    }

    // Download complete for retrieving Lyrics
    override fun downloadComplete(lyrics: String, song: MyParser.Song) {
        displayMessage(song.title)
        // Get the difficulty level and select appropriate kml to download
        val difficulty = settings.getString("difficulty", "2").toInt()
        DownloadKMLTask(this, lyrics, song).execute(contentUrl + "${song.number}/map$difficulty.kml")
    }

    // Download complete for retrieving kml. Lyrics and kml are passed to Maps Activity
    override fun downloadComplete(kmlString: String, lyrics: String, song: MyParser.Song) {
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
        // If the user guessed the song, add it to the list of completed songs
        if (requestCode == REQUEST_CODE && resultCode == SUCCESS) {
            val extras = data.extras
            val artist = extras.getString("ARTIST") as String
            val title = extras.getString("NAME") as String
            val link = extras.getString("LINK") as String
            val song = MyParser.Song("", artist, title, link)
            val gson = Gson()
            val jsonList = settings.getString("PLAYED", null)
            if (jsonList == null) {
                val playedList = ArrayList<MyParser.Song>()
                Log.v(TAG, "BEfore" + playedList.toString())
                playedList.add(song)
                val editor = settings.edit()
                val json = gson.toJson(playedList)
                editor.putString("PLAYED", json)
                editor.apply()
            } else {
                val type = object: TypeToken<ArrayList<MyParser.Song>>() {}.type
                val playedList = gson.fromJson<ArrayList<MyParser.Song>>(jsonList, type)
                Log.v(TAG, "BEfore" + playedList.toString())
                var seen = false
                for (s in playedList) {
                    if (s.title == song.title) {
                        seen = true
                    }
                }
                if (!seen) playedList.add(song)
                val editor = settings.edit()
                val json = gson.toJson(playedList)
                editor.putString("PLAYED", json)
                editor.apply()
            }
        }
    }
}
