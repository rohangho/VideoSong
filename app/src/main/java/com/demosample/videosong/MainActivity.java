package com.demosample.videosong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity extends AppCompatActivity {

    AppCompatTextView songOne;
    AppCompatTextView song2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songOne = findViewById(R.id.song1);
        song2 = findViewById(R.id.song2);
        songOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera(1);
            }
        });
        song2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera(2);
            }
        });
    }

    private void startCamera(int a) {
        if (a == 1)
            startActivity(new Intent(this, CameraActivity.class).putExtra("song", 1));
        else {
            startActivity(new Intent(this, CameraActivity.class).putExtra("song", 2));
        }
    }
}