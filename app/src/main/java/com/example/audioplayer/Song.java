package com.example.audioplayer;

import android.net.Uri;

public class Song {
    String title;
    Uri uri;
    Uri coverUri;
    int duration;
    String artist;


    public Song(String title, Uri uri, Uri coverUri, String artist, int duration) {
        this.title = title;
        this.uri = uri;
        this.artist = artist;
        this.coverUri = coverUri;
        this.duration = duration;
    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(Uri coverUri) {
        this.coverUri = coverUri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
