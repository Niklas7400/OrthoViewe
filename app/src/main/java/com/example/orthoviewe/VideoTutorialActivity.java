package com.example.orthoviewe;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoTutorialActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_tutorial);

        videoView = findViewById(R.id.videoViewTutorial);

        // MediaController für VideoView hinzufügen
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        playVideoTutorial();
    }

    private void playVideoTutorial() {
        // Setzen Sie die VideoView sichtbar und spielen Sie das lokale Video ab
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/raw/video_tutorial");
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
}
