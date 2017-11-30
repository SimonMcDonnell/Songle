package com.example.simonmcdonnell.songle

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.song_item.view.*

class CollectedLyricsAdapter(private val caller: Context, private var songList: ArrayList<String>) :
        RecyclerView.Adapter<CollectedLyricsAdapter.MyViewHolder>() {
    private val TAG = "LOG_TAG"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the song_item layout to use for each list item in recyclerview
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return CollectedLyricsAdapter.MyViewHolder(inflatedView, caller)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Once we bind to the song_item view, we assign its values
        holder.title.text = songList[position]
    }

    // Inner class to deal with a user clicking on an item in the song list
    class MyViewHolder(view: View, val caller: Context) : RecyclerView.ViewHolder(view) {
        private val TAG = "LOG_TAG"
        var title: TextView = view.song_title
    }
}