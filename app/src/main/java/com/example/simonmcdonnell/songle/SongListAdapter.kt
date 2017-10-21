package com.example.simonmcdonnell.songle

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.song_item.view.*

class SongListAdapter(val caller: SongClickedListener, var songList: ArrayList<MyParser.Song>) :
        RecyclerView.Adapter<SongListAdapter.MyViewHolder>() {
    private val TAG = "LOG_TAG"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the song_item layout to use for each list item in recyclerview
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongListAdapter.MyViewHolder(inflatedView, caller)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Once we bind to the song_item view, we assign its values
        val item = songList[position]
        holder.number = item.number
        holder.title.text = item.title
        holder.artist.text = item.artist
        holder.link = item.link
    }

    // Inner class to deal with a user clicking on an item in the song list
    class MyViewHolder(view: View, caller: SongClickedListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val TAG = "LOG_TAG"
        private val caller = caller
        var v: View = view
        var number = ""
        var title: TextView = v.song_title
        var artist: TextView = v.song_artist
        var link = ""

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Log.v(TAG, title.text.toString())
            caller.onClick(number)
        }
    }

    interface SongClickedListener {
        fun onClick(songNumber: String)
    }
}