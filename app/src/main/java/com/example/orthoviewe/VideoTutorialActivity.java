package com.example.orthoviewe;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoTutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_tutorial);

        VideoView videoView = findViewById(R.id.videoViewTutorial);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_tutorial); // Stellen Sie sicher, dass Sie die richtige Video-Ressource haben
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
}
