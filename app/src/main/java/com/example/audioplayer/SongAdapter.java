package com.example.audioplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Song> songs;

    ExoPlayer player;
    ConstraintLayout playerView;



    public SongAdapter(Context context, List<Song> songs, ExoPlayer player, ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player = player;
        this.playerView = playerView;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = (View )LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song song = songs.get(position);
        SongViewHolder viewHolder  = (SongViewHolder) holder;

        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.artistHolder.setText(song.getArtist());

        /*Uri songCover = song.getCoverUri();

        if(songCover != null)
        {
            viewHolder.symbolHolder.setImageURI(songCover);

            if(viewHolder.symbolHolder.getDrawable() == null)
            {
                viewHolder.symbolHolder.setImageResource(R.drawable.ic_baseline_headset_24);
            }
        }*/

        viewHolder.itemView.setOnClickListener(view ->{
            playerView.setVisibility(View.VISIBLE);

            if(!player.isPlaying())
            {
                player.setMediaItems(getMediaItems(),position,0);
            }
            else
            {
                player.pause();
                player.seekTo(position,0);
            }
            player.prepare();
            player.play();


            if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                ((MainListActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

    }

    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song: songs)
        {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetaData(song))
                    .build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetaData(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getCoverUri())
                .build();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView symbolHolder;
        TextView titleHolder,durationHolder, artistHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            symbolHolder = (ImageView) itemView.findViewById(R.id.symbol);
            titleHolder = (TextView) itemView.findViewById(R.id.title_view);
            durationHolder = (TextView) itemView.findViewById(R.id.duration);
            artistHolder = (TextView) itemView.findViewById(R.id.artist);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }



    @SuppressLint("DefaultLocale")
    private String getDuration(int totalDuration)
    {
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
}
