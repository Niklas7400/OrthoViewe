package com.example.orthoviewe;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
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
        Button btnRecordFront = findViewById(R.id.btnRecordFront);
        Button btnRecordSide = findViewById(R.id.btnRecordSide);
        Button btnViewVideo = findViewById(R.id.btnViewVideo);

        btnRecordFront.setOnClickListener(v -> {
            if (checkPermission()) {
                startRecording("front ");
            } else {
                requestPermission();
            }
        });

        btnRecordSide.setOnClickListener(v -> {
            if (checkPermission()) {
                startRecording("side ");
            } else {
                requestPermission();
            }
        });

        btnViewVideo.setOnClickListener(v -> showVideoFolders());
    }

    private void startRecording(String view) {
        String folderName = editTextFileName.getText().toString().trim();
        if (folderName.isEmpty()) {
            Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile;
            try {
                videoFile = createVideoFile(view, folderName);
            } catch (IOException ex) {
                Toast.makeText(this, "Error occurred while creating the video file", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return;
            }

            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this, "com.example.orthoviewe.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private File createVideoFile(String view, String folderName) throws IOException {
        File storageDir = new File(getExternalFilesDir(null), folderName);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + storageDir.getAbsolutePath());
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = view + "VIDEO_" + timeStamp;
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
                // Check which button was clicked last
                // This part is a bit tricky, so ensure you handle the permission result properly
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showVideoFolders() {
        File storageDir = getExternalFilesDir(null);
        File[] folders = storageDir.listFiles(File::isDirectory);

        if (folders != null && folders.length > 0) {
            ArrayList<String> folderNames = new ArrayList<>();
            for (File folder : folders) {
                folderNames.add(folder.getName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a folder to view videos");

            // Create a view for the search functionality
            View searchView = getLayoutInflater().inflate(R.layout.dialog_search, null);
            SearchView search = searchView.findViewById(R.id.searchView);
            ListView listView = searchView.findViewById(R.id.listView);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, folderNames);
            listView.setAdapter(adapter);

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });

            listView.setOnItemClickListener((parent, view, position, id) -> showVideosInFolder(new File(storageDir, adapter.getItem(position))));

            builder.setView(searchView);
            builder.show();
        } else {
            Toast.makeText(this, "No folders found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showVideosInFolder(File folder) {
        File[] videoFiles = folder.listFiles((dir, name) -> name.endsWith(".mp4"));

        if (videoFiles != null && videoFiles.length > 0) {
            ArrayList<String> videoFileNames = new ArrayList<>();
            for (File file : videoFiles) {
                videoFileNames.add(file.getName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a video to view");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoFileNames);
            builder.setItems(videoFileNames.toArray(new String[0]), (dialog, which) -> playVideo(videoFiles[which]));
            builder.show();
        } else {
            Toast.makeText(this, "No videos found in this folder!", Toast.LENGTH_SHORT).show();
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
