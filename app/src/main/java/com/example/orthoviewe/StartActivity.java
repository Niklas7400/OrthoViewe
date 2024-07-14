package com.example.orthoviewe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button btnOpenApp = findViewById(R.id.btnOpenApp);
        Button btnOpenStorage = findViewById(R.id.btnOpenStorage);
        Button btnTutorial = findViewById(R.id.btnVideoTutorial); // Korrigierte ID

        btnOpenApp.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnOpenStorage.setOnClickListener(v -> {
            showVideoFolders();
        });

        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, VideoTutorialActivity.class);
            startActivity(intent);
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
            builder.setTitle("Ordner auswählen, um Videos anzuzeigen");

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
            Toast.makeText(this, "Keine Ordner gefunden!", Toast.LENGTH_SHORT).show();
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
            builder.setTitle("Video auswählen, um es anzusehen");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoFileNames);
            builder.setItems(videoFileNames.toArray(new String[0]), (dialog, which) -> {
                playVideo(videoFiles[which]);
            });
            builder.show();
        } else {
            Toast.makeText(this, "Keine Videos in diesem Ordner gefunden!", Toast.LENGTH_SHORT).show();
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
