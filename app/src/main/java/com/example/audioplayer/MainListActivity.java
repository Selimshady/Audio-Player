package com.example.audioplayer;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MainListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> songs;

    ExoPlayer player;
    ConstraintLayout playerView;
    TextView playerCloseBtn;

    TextView songNameView, skipPreviousBtn, skipNextBtn, playPauseBtn, repeatModeBtn,playListBtn;
    TextView homeSongNameView, homeSkipPreviousBtn, homePlayPauseBtn, homeSkipNextBtn;

    ConstraintLayout homeControlWrapper, headWrapper, seekbarWrapper, controlWrapper, audioVisualizerWrapper;
    ImageView artworkView;

    SeekBar seekbar;
    TextView progressView, durationView;
    BarVisualizer audioVisualizer;

    int defaultStatusColor;
    int repeatMode = 1; // repeat all  = 1 , repeat one = 2 , shuffle = 3

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        songs = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.songList);
        player = new ExoPlayer.Builder(this).build();



        defaultStatusColor = getWindow().getStatusBarColor();

        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            fetchSongs();
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
        {
            if(player.isPlaying()) {
                activateAudioVisualizer();
            }
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 12);
        }


        songs.sort(new Comparator<Song>() {
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        playerView = findViewById(R.id.player_view);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playListBtn = findViewById(R.id.playlistBtn);

        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreviousBtn = findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);

        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        seekbarWrapper = findViewById(R.id.seekbarWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

        artworkView = findViewById(R.id.artworkView);
        seekbar = findViewById(R.id.seekBar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.duration);
        audioVisualizer = findViewById(R.id.visualizer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        songAdapter = new SongAdapter(this,songs,player,playerView);
        recyclerView.setAdapter(songAdapter);


        playerControls();
    }

    private void playerControls()
    {
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);


        //exit the player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playListBtn.setOnClickListener(view -> exitPlayerView());
        //open player view on  home control wrapper click
        homeControlWrapper.setOnClickListener(view -> showPlayerView());


        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);

                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
                seekbar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_circle,0,0,0);
                homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_circle,0,0,0);

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
            }
        });


    }

    @SuppressLint("DefaultLocale")
    private String getReadableTime(int currentPosition) {
        String currentPositionText;

        int hrs = currentPosition/(1000*60*60);
        int min = (currentPosition%(1000*60*60))/(1000*60);
        int secs = (((currentPosition%(1000*60*60))%(1000*60*60))%(1000*600))/10000;

        if(hrs<1)
        {
            currentPositionText = String.format("%2d:%2d",min,secs);
        }
        else
        {
            currentPositionText = String.format("%1d:%2d:%2d", hrs,min,secs);
        }

        return currentPositionText;
    }

    private void showPlayerView()
    {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }

    private void updatePlayerColors()
    {

    }

    private void exitPlayerView()
    {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));

    }

    private void activateAudioVisualizer()
    {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player.isPlaying())
        {
            player.stop();
        }
        player.release();
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
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        try
        {
            Cursor cursor = getContentResolver().query(mediaStoreUri, projection,MediaStore.Audio.Media.IS_MUSIC + " != 0", null,null);

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while(cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                String artist = cursor.getString(artistColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

                Song song = new Song(name,uri,albumCoverUri,artist,duration);

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
            if(requestCode == 11)
                fetchSongs();
            else if(requestCode == 12)
                activateAudioVisualizer();
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }
}