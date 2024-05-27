package com.example.orthoviewe;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private String currentVideoPath;
    private EditText editTextFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFileName = findViewById(R.id.name);
        Button btnRecordVideo = findViewById(R.id.btnRecordVideo);
        Button btnViewVideo = findViewById(R.id.btnViewVideo);

        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    startRecording();
                } else {
                    requestPermission();
                }
            }
        });

        btnViewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoFiles();
            }
        });
    }

    private void startRecording() {
        String fileName = editTextFileName.getText().toString().trim();
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile(fileName);
            } catch (IOException ex) {
                Toast.makeText(this, "Error occurred while creating the video file", Toast.LENGTH_SHORT).show();
            }

            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this, "com.example.orthoviewe.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private File createVideoFile(String fileName) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = fileName + "_" + timeStamp;
        File storageDir = getExternalFilesDir(null);
        File video = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );
        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showVideoFiles() {
        File storageDir = getExternalFilesDir(null);
        File[] videoFiles = storageDir.listFiles((dir, name) -> name.endsWith(".mp4"));

        if (videoFiles != null && videoFiles.length > 0) {
            ArrayList<String> videoFileNames = new ArrayList<>();
            for (File file : videoFiles) {
                videoFileNames.add(file.getName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a video to view");
            builder.setItems(videoFileNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    playVideo(videoFiles[which]);
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, "No videos found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void playVideo(File videoFile) {
        Uri videoURI = FileProvider.getUriForFile(this, "com.example.orthoviewe.fileprovider", videoFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(videoURI, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
