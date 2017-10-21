package com.example.simonmcdonnell.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set on click listener for play button
        play_button.setOnClickListener { View ->
            val songListIntent = Intent(this, SongListActivity::class.java)
            startActivity(songListIntent)
        }
    }
}
