package com.javafx.songmanager.models;

import javax.persistence.*;

@Entity
@Table(name = "SongPath")
public class SongPath {
    @Id
    private int id;

    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}