package com.example.audioplayer;

import android.annotation.SuppressLint;
import android.content.Context;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Song> songs;



    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
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
        viewHolder.durationHolder.setText(String.valueOf(song.getDuration()));
        viewHolder.sizeHolder.setText(String.valueOf(song.getSize()));

        Uri songCover = song.getCoverUri();

        if(songCover != null)
        {
            viewHolder.symbolHolder.setImageURI(songCover);

            if(viewHolder.symbolHolder.getDrawable() == null)
            {
                viewHolder.symbolHolder.setImageResource(R.drawable.ic_baseline_headset_24);
            }
        }

        viewHolder.itemView.setOnClickListener(view -> Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show());

    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView symbolHolder;
        TextView titleHolder,durationHolder,sizeHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            symbolHolder = (ImageView) itemView.findViewById(R.id.symbol);
            titleHolder = (TextView) itemView.findViewById(R.id.title_view);
            durationHolder = (TextView) itemView.findViewById(R.id.duration);
            sizeHolder = (TextView) itemView.findViewById(R.id.size);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Song> filteredList)
    {
        songs = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
