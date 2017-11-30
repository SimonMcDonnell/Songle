package com.example.simonmcdonnell.songle

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_completed.*

class CompletedActivity : AppCompatActivity(), CompletedAdapter.CollectedItemClickListener {
    private val TAG = "LOG_TAG"
    private lateinit var songList: ArrayList<MyParser.Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed)
        supportActionBar?.title = "Completed Songs"
        // Get list of solved songs from Shared Prefs
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val jsonList = settings.getString("PLAYED", null)
        val type = object: TypeToken<ArrayList<MyParser.Song>>() {}.type
        val playedList = gson.fromJson<ArrayList<MyParser.Song>>(jsonList, type)
        songList = ArrayList()
        if (playedList != null) songList.addAll(playedList)
        // display completed list
        val layout = recyclerview
        val layoutManager = LinearLayoutManager(this)
        // Add line between each item
        val decoration = DividerItemDecoration(layout.context, layoutManager.orientation)
        layout.layoutManager = layoutManager
        layout.addItemDecoration(decoration)
        layout.adapter = CompletedAdapter(this, songList)
    }

    override fun onItemClick(id: Int) {
        // Listen to song on youtube when item is clicked
        val song = songList[id]
        val link = song.link
        val youTubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(youTubeIntent)
    }
}
