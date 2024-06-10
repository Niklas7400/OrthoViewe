package com.example.orthoviewe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button btnOpenApp = findViewById(R.id.btnOpenApp);
        Button btnOpenStorage = findViewById(R.id.btnOpenStorage);

        btnOpenApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnOpenStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoFolders();
            }
        });
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

            listView.setOnItemClickListener((parent, view, position, id) -> {
                showVideosInFolder(new File(storageDir, adapter.getItem(position)));
            });

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
            builder.setItems(videoFileNames.toArray(new String[0]), (dialog, which) -> {
                playVideo(videoFiles[which]);
            });
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