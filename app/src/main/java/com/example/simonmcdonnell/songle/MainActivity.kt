package com.example.simonmcdonnell.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        play_button.setOnClickListener { View ->
            val toast = Toast.makeText(this, "Go to Maps Activity", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}
