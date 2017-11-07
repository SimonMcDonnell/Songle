package com.example.simonmcdonnell.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_song_list.*

class ReviewActivity : AppCompatActivity() {
    private val TAG = "LOG_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        supportActionBar?.title = "Completed Songs"
        // Get list of solved songs
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val jsonList = settings.getString("PLAYED", null)
        val type = object: TypeToken<ArrayList<MyParser.Song>>() {}.type
        val playedList = gson.fromJson<ArrayList<MyParser.Song>>(jsonList, type)
        val songList = ArrayList<String>()
        if (playedList != null) {
            playedList.forEach { songList.add("${it.title} - ${it.artist}") }
        }
        val layout = recyclerview
        layout.layoutManager = LinearLayoutManager(this)
        layout.adapter = SongListAdapter(this, songList)
    }
}
