package com.example.mobivideoplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobivideoplayer.activities.MobiVideoPlayer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hello = ArrayList<String>()
        hello.add("/storage/emulated/0/DCIM/Y2Mate.is - Extreme Parkour and Freerunning-oIJ5m1_6E24-720p-1656942636636.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/1.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/2.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/3.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/4.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/5.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/6.mp4")
        hello.add("/storage/emulated/0/DCIM/Video Editor/7.mp4")

        val intent = Intent(this, MobiVideoPlayer::class.java)
        intent.putExtra("position", 0)
        intent.putStringArrayListExtra("videoArrayList", hello)
        startActivity(intent)
    }
}