package com.javafx.songmanager.utils;

import com.javafx.songmanager.models.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.HashMap;
import java.util.UUID;

public class SessionManager {
    private HashMap<String, UserInfo> sessions;
    private Key key;

    public SessionManager() {
        sessions = new HashMap<>();
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // generate a random key
    }

    public String createSession(UserInfo user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);

        String jws = Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(key)
                .compact();

        return jws;
    }

    public void destroySession(String sessionId) {
        sessions.remove(sessionId);
    }

    public UserInfo getUserFromSession(String sessionId) {
        return sessions.get(sessionId);
    }
}