package com.example.simonmcdonnell.songle

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_guessed.*

class GuessedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessed)
        supportActionBar?.title = "Congratulations! +40XP"
        // Get extras passed to this activity
        val extras = intent.extras
        guessed_title.text = extras["NAME"] as String
        guessed_artist.text = extras["ARTIST"] as String
        guessed_lyrics.text = extras["LYRICS"] as String
        // Register on click for button to link to YouTube
        youtube_button.setOnClickListener { _ ->
            val link = extras["LINK"] as String
            val youTubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(youTubeIntent)
        }
    }
}
