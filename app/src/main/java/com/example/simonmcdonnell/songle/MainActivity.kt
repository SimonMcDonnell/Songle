package com.example.simonmcdonnell.songle

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
    private val lyricsUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"
    private val mapsUrl = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set on click listener for play button
        play_button.setOnClickListener { _ ->
            val haveConnection = checkConnection()
            if (haveConnection) {
                DownloadXMLTask(this).execute(songsUrl)
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
        DownloadTXTTask(this, song).execute(lyricsUrl + "${song.number}/lyrics.txt")
    }

    override fun downloadComplete(lyrics: String, song: MyParser.Song) {
        displayToast(song.title)
        Log.v(TAG, "Here is the lyrics $lyrics")
        DownloadKMLTask(this, lyrics, song).execute(mapsUrl + "${song.number}/map1.kml")
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
