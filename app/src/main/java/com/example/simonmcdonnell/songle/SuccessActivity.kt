package com.example.simonmcdonnell.songle

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_success.*

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        // Check difficulty and if timed mode is enabled to determine XP gained
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val difficulty = settings.getString("difficulty", "3").toInt()
        var reward = 0
        when (difficulty) {
            1 -> reward += 25
            2 -> reward += 20
            3 -> reward += 15
            4 -> reward += 10
            5 -> reward += 5
        }
        // If timed mode was on, add an additional 10XP to reward
        val timed = settings.getBoolean("timer", false)
        if (timed) reward += 10
        // Display XP gained in action bar
        supportActionBar?.title = "Congratulations! +${reward}XP"
        // Get song passed to this activity
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
