package com.demosample.videosong

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView

class MainActivity : AppCompatActivity() {
    private lateinit var songOne: AppCompatTextView
    private lateinit var song2: AppCompatTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        songOne = findViewById(R.id.song1)
        song2 = findViewById(R.id.song2)
        songOne.setOnClickListener(View.OnClickListener { startCamera(1) })
        song2.setOnClickListener(View.OnClickListener { startCamera(2) })
    }

    private fun startCamera(a: Int) {
        if (a == 1) startActivity(Intent(this, CameraActivity::class.java).putExtra("song", 1)) else {
            startActivity(Intent(this, CameraActivity::class.java).putExtra("song", 2))
        }
    }
}