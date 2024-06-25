package com.javafx.songmanager.models;

import javax.persistence.*;

@Entity
@Table(name = "Song")
public class Song {
    @Id
    private int id;

    private String title;

    private String artist;

    private String album;

    private String releaseYear;

    public Song() {
    }

    public Song(int id, String title, String artist, String album, String releaseYear) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.releaseYear = releaseYear;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }
}
