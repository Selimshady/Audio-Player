package com.example.audioplayer;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;


import android.util.Log;
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
import java.util.Objects;


public class MainListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();

    ActivityResultLauncher<String> storagePermissionLauncher;
    ActivityResultLauncher<String> recordAudioPermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;

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

    boolean isBound = false;

    int defaultStatusColor;
    int repeatMode = 1; // repeat all  = 1 , repeat one = 2 , shuffle = 3

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        defaultStatusColor = getWindow().getStatusBarColor();
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));

        recyclerView = (RecyclerView) findViewById(R.id.songList);

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{
            if(granted)
                fetchSongs();
            else
                userResponse();
        });

        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{
            if(granted && player.isPlaying()) {
                activateAudioVisualizer();
            }
            else
                userResponsesOnRecordAudioPerm();
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
        durationView = findViewById(R.id.durationView);
        audioVisualizer = findViewById(R.id.visualizer);

        doBindService();
    }

    private void userResponsesOnRecordAudioPerm() {
          if(shouldShowRequestPermissionRationale(recordAudioPermission)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("Audio Record permission is needed to show you audio visualizer.")
                        .setPositiveButton("ok", (dialogInterface, i) -> recordAudioPermissionLauncher.launch(recordAudioPermission))
                        .setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
          }
    }

    private void userResponse() {
        if(ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED)
        {
            fetchSongs();
        }
        else if(shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Store permission is needed to reach your songs and show them in this app")
                    .setPositiveButton("ok", (dialogInterface, i) -> storagePermissionLauncher.launch(permission))
                    .setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
    }


    private void doBindService() {
        Intent playerServiceIntent = new Intent(this,PlayerService.class);
        startService(new Intent(this, PlayerService.class));
        bindService(playerServiceIntent,playerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.ServiceBinder binder = (PlayerService.ServiceBinder) iBinder;
            player = binder.getPlayerService().player;
            isBound = true;

            storagePermissionLauncher.launch(permission);
            playerControls();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void fetchSongs() {
        Uri mediaStoreUri;
        List<Song> songs = new ArrayList<>();


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
            Cursor cursor = getContentResolver().query(mediaStoreUri, projection,null, null,null);

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

            songs.sort(Comparator.comparing(Song::getTitle));
            showSongs(songs);
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showSongs(List<Song> songs) {
        if(songs.size() == 0)
        {
            Toast.makeText(this, "No Songs", Toast.LENGTH_SHORT).show();
            return;
        }
        allSongs.clear();
        allSongs.addAll(songs);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        songAdapter = new SongAdapter(this, allSongs,player,playerView);
        recyclerView.setAdapter(songAdapter);


    }

    @Override
    public void onBackPressed() {
        if(playerView.getVisibility() == View.VISIBLE)
        {
            exitPlayerView();
        }
        else
            super.onBackPressed();
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
                homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                showCurrentArtwork();

                updatePlayerPositionProgress();


                activateAudioVisualizer();

                if(!player.isPlaying())
                {
                    player.play();
                }

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == ExoPlayer.STATE_READY)
                {
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekbar.setMax((int) player.getDuration());
                    seekbar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_circle,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);


                    showCurrentArtwork();

                    updatePlayerPositionProgress();

                    activateAudioVisualizer();
                }
                else
                {
                    playPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play_circle,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });

        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());

        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homeSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());

        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekbar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player.getPlaybackState() == ExoPlayer.STATE_READY)
                {
                    seekbar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        repeatModeBtn.setOnClickListener(view -> {
            if(repeatMode == 1)
            {
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 2;
                repeatModeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
            }
            else if(repeatMode == 2)
            {
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_shuffle,0,0,0);
            }
            else
            {
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_repeat,0,0,0);
            }
        });


    }

    private void playOrPausePlayer()
    {
        if(player.isPlaying())
        {
            player.pause();
            playPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play_circle,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
        }
        else
        {
            player.play();
            playPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_circle,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
        }

    }

    private void skipToPreviousSong()
    {
        if(player.hasPreviousMediaItem())
        {
            player.seekToPrevious();
        }
        else
        {
            player.seekTo(allSongs.size());
        }
    }

    private void skipToNextSong()
    {
        if(player.hasNextMediaItem())
        {
            player.seekToNext();
        }
        else
        {
            player.seekTo(0);
        }
    }

    private void updatePlayerPositionProgress()
    {
        new Handler().postDelayed(() -> {

            if(player.isPlaying())
            {
                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
            }

            updatePlayerPositionProgress();
        },1000);
    }

    private void showCurrentArtwork()
    {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if(artworkView.getDrawable() == null)
        {
            artworkView.setImageResource(R.drawable.ic_baseline_headset_24);
        }
    }

    @SuppressLint("DefaultLocale")
    private String getReadableTime(int totalDuration) {
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = (((totalDuration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if(hrs<1)
        {
            totalDurationText = String.format("%02d:%02d",min,secs);
        }
        else
        {
            totalDurationText = String.format("%01d:%02d:%02d", hrs,min,secs);
        }

        return totalDurationText;
    }

    private void showPlayerView()
    {
        playerView.setVisibility(View.VISIBLE);
    }


    private void exitPlayerView()
    {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));

    }

    private void activateAudioVisualizer()
    {
        if(ContextCompat.checkSelfPermission(this,recordAudioPermission) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        audioVisualizer.setColor(ContextCompat.getColor(this,R.color.turquoise));
        audioVisualizer.setDensity(10);
        audioVisualizer.setPlayer(player.getAudioSessionId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }


    private void doUnbindService() {
        if(isBound)
        {
            unbindService(playerServiceConnection);
            isBound = false;
        }
    }

}