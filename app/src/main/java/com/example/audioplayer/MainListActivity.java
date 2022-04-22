package com.example.audioplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;

import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class MainListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> songs;
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        songs = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.songList);

        if(songs.size()==0){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                fetchSongs();
            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);
            }
        }

        songAdapter = new SongAdapter(this,songs);
        recyclerView.setAdapter(songAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void userResponse() {
        if(ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED)
        {
            fetchSongs();
        }
        else if(shouldShowRequestPermissionRationale(permission))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Requesting Permission")
                    .setMessage("Allow us to fetch songs  on your device")
                    .setPositiveButton("allow", (dialogInterface, i) -> storagePermissionLauncher.launch(permission))
                    .setNegativeButton("cancel", (dialogInterface, i) -> {
                        Toast.makeText(getApplicationContext(), "You denied us to show songs",Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    })
                    .show();
        }
        else
        {
            Toast.makeText(this, "You canceled to show songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {
        Uri mediaStoreUri;

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q)
        {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }
        else
        {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        try
        {
            Cursor cursor = getContentResolver().query(mediaStoreUri, projection,MediaStore.Audio.Media.IS_MUSIC + " != 0", null,null);

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while(cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

                name = name.substring(0,name.lastIndexOf("."));

                Song song = new Song(name,uri,albumCoverUri,size,duration);

                songs.add(song);

            }

            cursor.close();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }


}