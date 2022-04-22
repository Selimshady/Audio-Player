package com.example.audioplayer;

import android.net.Uri;

public class Song {
    String title;
    Uri uri;
    Uri coverUri;
    int size;
    int duration;


    public Song(String title, Uri uri, Uri coverUri, int size, int duration) {
        this.title = title;
        this.uri = uri;
        this.coverUri = coverUri;
        this.size = size;
        this.duration = duration;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
