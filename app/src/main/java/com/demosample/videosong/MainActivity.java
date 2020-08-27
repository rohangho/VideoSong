package com.demosample.videosong;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayer mediaPlayer = new MediaPlayer();
        AssetFileDescriptor afd;
        try {
            afd = getAssets().openFd("a.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Playing" + "a",
                Toast.LENGTH_SHORT).show();
    }
}