package com.javafx.songmanager.controllers;

import javafx.fxml.FXML;

public class UserAppController {
    private String sessionId;
    private String username;


    @FXML
    void initialize(){};

    public UserAppController() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
