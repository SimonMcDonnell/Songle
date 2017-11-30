package com.example.simonmcdonnell.songle

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.completed_song_item.view.*

class CompletedAdapter(private val caller: CollectedItemClickListener, private var songList: ArrayList<MyParser.Song>) :
        RecyclerView.Adapter<CompletedAdapter.MyViewHolder>() {
    private val TAG = "LOG_TAG"

    interface CollectedItemClickListener {
        fun onItemClick(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the song_item layout to use for each list item in recyclerview
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.completed_song_item, parent, false)
        return CompletedAdapter.MyViewHolder(inflatedView, caller)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Once we bind to the completed_song_item view, we assign its text
        val song = songList[position]
        holder.title.text = "${song.title} - ${song.artist}"
        holder.pos = position
    }

    // Inner class to deal with a user clicking on an item in the song list
    class MyViewHolder(view: View, private val caller: CollectedItemClickListener) :
            RecyclerView.ViewHolder(view), View.OnClickListener{
        private val TAG = "LOG_TAG"
        val title: TextView = view.song_description
        var pos = -1

        init {
            view.setOnClickListener(this)
        }

        // On Click listener for when using clicks item
        override fun onClick(p0: View?) {
            caller.onItemClick(pos)
        }
    }

}