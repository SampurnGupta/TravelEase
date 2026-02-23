package com.example.travelease;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find VideoView
        VideoView videoView = findViewById(R.id.videoView);

        // Set Video Background
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/raw/background_video");
        videoView.setVideoURI(videoUri);
        videoView.start();

        // Delay for 4 seconds, then go to LoginActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 4000); // 4 seconds
    }
}
