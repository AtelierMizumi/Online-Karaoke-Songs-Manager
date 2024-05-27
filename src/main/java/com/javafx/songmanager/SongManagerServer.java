package com.javafx.songmanager;

import com.javafx.songmanager.models.Song;
import com.javafx.songmanager.models.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class SongManagerServer {
    private static final String PERSISTENCE_UNIT_NAME = "databasePU";
    private SessionFactory sessionFactory;

    public SongManagerServer() {
        javax.persistence.EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("databasePU");
        sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    public static void main(String[] args) {
        SongManagerServer server = new SongManagerServer();
        try {
            server.startServer();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    public void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("SongManager Server started on port 8080");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New connection from " + clientSocket.getInetAddress());

            HandleClientRequestThread thread = new HandleClientRequestThread(clientSocket, this);
            thread.start();
        }
    }

    private class HandleClientRequestThread extends Thread {
        private Socket clientSocket;
        private SongManagerServer server;

        public HandleClientRequestThread(Socket clientSocket, SongManagerServer server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String request = reader.readLine();
                System.out.println("Received request: " + request);

                String[] parts = request.split(" ");

                if (parts[0].equals("REGISTER")) {
                    handleRegister(parts, writer);
                } else if (parts[0].equals("LOGIN")) {
                    handleLogin(parts, writer);
                } else if (parts[0].equals("CREATE_SONG")) {
                    handleCreateSong(parts, writer);
                } else if (parts[0].equals("READ_SONGS")) {
                    handleReadSongs(writer);
                } else if (parts[0].equals("UPDATE_SONG")) {
                    handleUpdateSong(parts, writer);
                } else if (parts[0].equals("DELETE_SONG")) {
                    handleDeleteSong(parts, writer);
                } else {
                    writer.println("ERROR: Invalid request");
                }
            } catch (Exception e) {
                System.err.println("Error handling client request: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    private void handleRegister(String[] parts, PrintWriter writer) {
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];

        // BCrypt
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);

        UserInfo userInfo = new UserInfo(email, username, hashedPassword, salt, false);
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.save(userInfo);
        session.getTransaction().commit();

        writer.println("REGISTER_OK");
        System.out.println("User registered: " + username);
    }

    private void handleLogin(String[] parts, PrintWriter writer) {
        String username = parts[1];
        String password = parts[2];

        // logic to query user's information
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM UserInfo WHERE username = :username");
        query.setParameter("username", username);
        UserInfo userInfo = (UserInfo) query.getSingleResult();
        session.getTransaction().commit();
        session.close();

        if (userInfo != null) {
                if (BCrypt.checkpw(password, userInfo.getHashed_password())) {
                    String sessionId = generateSessionId(userInfo);
                    writer.println("LOGIN_OK " + sessionId);
            } else {
                writer.println("LOGIN_FAILED PASSWORD_INCORRECT");
            }
        } else {
            writer.println("LOGIN_FAILED USER_NON_EXIST");
        }

    }

    private void handleCreateSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        String title = parts[2];
        String artist = parts[3];
        String album = parts[4];
        String year = parts[5];
        String path = parts[6];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Song song = new Song(title, artist, album, year, path);
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            session.save(song);
            session.getTransaction().commit();

            writer.println("CREATE_SONG_OK");
        } else {
            writer.println("CREATE_SONG_FAIL " + sessionId);
        }
    }

    private UserInfo getSessionUserInfo(String sessionId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        UserInfo userInfo = session.get(UserInfo.class, sessionId);
        session.getTransaction().commit();
        return userInfo;
    }

    private void handleReadSongs(PrintWriter writer) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<Song> songs = session.createQuery("FROM Song").list();
        session.getTransaction().commit();

        for (Song song : songs) {
            writer.println("SONG " + song.getTitle() + " " + song.getArtist() + " " + song.getAlbum() + " " + song.getReleased_year() + " " + song.getPath());
            writer.println("END_SONGS");
            session.getTransaction().commit();
        }
}

    private void handleUpdateSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        String title = parts[2];
        String artist = parts[3];
        String album = parts[4];
        String year = parts[5];
        String path = parts[6];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Song song = session.get(Song.class, title);
            if (song != null) {
                song.setArtist(artist);
                song.setAlbum(album);
                song.setReleased_year(year);
                song.setPath(path);
                session.update(song);
                session.getTransaction().commit();

                writer.println("UPDATE_SONG_OK");
            } else {
                writer.println("UPDATE_SONG_FAIL");
            }
        } else {
            writer.println("UPDATE_SONG_FAIL");
        }
    }

    private void handleDeleteSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        String title = parts[2];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Song song = session.get(Song.class, title);
            if (song != null) {
                session.delete(song);
                session.getTransaction().commit();

                writer.println("DELETE_SONG_OK");
            } else {
                writer.println("DELETE_SONG_FAIL");
            }
        } else {
            writer.println("DELETE_SONG_FAIL");
        }
    }

    private String generateSessionId(UserInfo userInfo) {
        // implement your own session ID generation logic
        return UUID.randomUUID().toString();
    }
}