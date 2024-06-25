package com.javafx.songmanager;

import com.javafx.songmanager.models.Song;
import com.javafx.songmanager.models.SongPath;
import com.javafx.songmanager.models.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.parseInt;

public class SongManagerServer {
    private static final String PERSISTENCE_UNIT_NAME = "databasePU";
    private SessionFactory sessionFactory;
    private ConcurrentHashMap<String, UserInfo> clientSessions = new ConcurrentHashMap<>();

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

    public synchronized void startServer() throws IOException {
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

                String[] parts = request.split("\\|", -1);

                switch (parts[0]) {
                    case "REGISTER" -> handleRegister(parts, writer);
                    case "LOGIN" -> handleLogin(parts, writer);
                    case "CREATE_SONG" -> handleCreateSong(parts, writer);
                    case "ADD_SONG_MEDIA_TO_ID" -> handleAddSongMediaToId(parts, clientSocket, writer);
                    case "RETRIEVE_SONGS" -> handleRetrieveSongs(parts, writer);
                    case "UPDATE_SONG" -> handleUpdateSong(parts, writer);
                    case "DELETE_SONG" -> handleDeleteSong(parts, writer);
                    case "SEND_MESSAGE" -> handleSendMessage(parts, writer);
                    case "GET_USER_INFO" -> handleReturnUserCredentials(parts, writer);
                    case "GET_MEDIA" -> handleGetMedia(parts, writer);
                    default -> writer.println("ERROR: Invalid request");
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

        public void handleReturnUserCredentials(String[] parts, PrintWriter writer) {
            String sessionId = parts[1];
            UserInfo userInfo = server.getSessionUserInfo(sessionId);
            if (userInfo != null) {
                writer.println("GET_USER_INFO_OK|" + userInfo.getEmail_Address() + "|" + userInfo.getUsername());
            } else {
                writer.println("GET_USER_INFO_FAIL|INVALID_SESSION_ID");
            }
        }

        private void handleSendMessage(String[] parts, PrintWriter writer) {
            String sessionId = parts[1];
            String message = parts[2];

            UserInfo userInfo = server.getSessionUserInfo(sessionId);
            if (userInfo != null) {
                // logic to save the message to the database

                // Iterate over all connected clients and send the message to each of them
                for (Socket clientSocket : server.getClients()) {
                    try {
                        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        clientWriter.println("MESSAGE|" + sessionId + "|" + message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                writer.println("SEND_MESSAGE_OK");
            } else {
                writer.println("SEND_MESSAGE_FAIL");
            }
        }
    }

    private void handleGetMedia(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        int songId = parseInt(parts[2]);

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            SongPath songPath = session.get(SongPath.class, songId);
            if (songPath != null) {
                File file = new File(songPath.getPath());
                if (file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[16 * 1024];
                        int count;
                        while ((count = fis.read(buffer)) > 0) {
                            writer.println(new String(buffer, 0, count));
                        }
                        fis.close();
                        writer.println("GET_MEDIA_OK");
                    } catch (IOException e) {
                        writer.println("GET_MEDIA_FAIL");
                        throw new RuntimeException(e);
                    }
                } else {
                    writer.println("NO_MEDIA_FOUND");
                }
            } else {
                writer.println("GET_MEDIA_FAIL");
            }
        }
    }

    private Socket[] getClients() {
        return new Socket[0];
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
        Session transactionSession = sessionFactory.getCurrentSession();
        transactionSession.beginTransaction();
        Query query = transactionSession.createQuery("FROM UserInfo WHERE username = :username");
        query.setParameter("username", username);
        UserInfo userInfo = (UserInfo) query.getSingleResult();
        transactionSession.getTransaction().commit();
        transactionSession.close();

        if (userInfo != null) {
                if (BCrypt.checkpw(password, userInfo.getHashed_password())) {
                    if (userInfo.isAdmin()) {
                        String sessionId = generateSessionId();
                        clientSessions.put(sessionId, userInfo);
                        writer.println("LOGIN_OK_ADMIN|" + sessionId);
                    } else {
                        // generate a session ID and save it to the clientSessions map (session ID, user info
                        String sessionId = generateSessionId();
                        clientSessions.put(sessionId, userInfo);
                        writer.println("LOGIN_OK|" + sessionId);
                    }
                } else {
                    writer.println("LOGIN_FAILED|PASSWORD_INCORRECT");
                }
        } else {
            writer.println("LOGIN_FAILED|USER_NON_EXIST");
        }

    }

    private void handleCreateSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        String title = parts[2];
        String artist = parts[3];
        String album = parts[4];
        String year = parts[5];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Song song = new Song(generateSongId(), title, artist, album, year);
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            session.save(song);
            session.flush();
            session.getTransaction().commit();

            writer.println("CREATE_SONG_OK|" + song.getId());
        } else {
            writer.println("CREATE_SONG_FAIL|INVALID_SESSION_ID");
        }
    }
    private void handleAddSongMediaToId(String[] parts, Socket clientSocket, PrintWriter writer) {
        String sessionId = parts[1];
        int songId = parseInt(parts[2]);

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            try {
                // Receive the file
                byte[] bytes = new byte[16 * 1024];
                InputStream in = clientSocket.getInputStream();
                String randomHashName = UUID.randomUUID().toString();
                String newFilePath = "/Songs/" + randomHashName;
                FileOutputStream fos = new FileOutputStream(newFilePath);
                int count;
                while ((count = in.read(bytes)) > 0) {
                    fos.write(bytes, 0, count);
                }

                // Update the SongPath field in the database
                Session session = sessionFactory.getCurrentSession();
                session.beginTransaction();
                Song song = session.get(Song.class, songId);
                if (song != null) {
                    SongPath songPath = new SongPath();
                    songPath.setId(songId);
                    songPath.setPath(newFilePath);

                    session.save(songPath);
                    session.getTransaction().commit();

                    writer.println("UPLOAD_SONG_MEDIA_OK");
                } else {
                    writer.println("UPLOAD_SONG_MEDIA_FAIL");
                }
            } catch (IOException e) {
                writer.println("UPLOAD_SONG_MEDIA_FAIL");
                throw new RuntimeException(e);
            }
        } else {
            writer.println("UPLOAD_SONG_MEDIA_FAIL");
        }
    }

    private UserInfo getSessionUserInfo(String sessionId) {
        // logic to get the user info from the session ID
        return clientSessions.get(sessionId);
    }

    private void handleRetrieveSongs(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        System.out.println("Retrieving songs for session ID: " + sessionId);
        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo == null) {
            writer.println("RETRIEVE_SONGS_FAIL|NULL_SESSION_ID");
            return;
        } else {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM Song");
            List<Song> songs = query.getResultList();
            session.getTransaction().commit();

            for (Song song : songs) {
                String songDetails = String.format("%s|%s|%s|%s|%s", song.getId(), song.getTitle(), song.getArtist(), song.getAlbum(), song.getReleaseYear());
                writer.println(songDetails);
            }
            writer.println("END_SONGS");
        }
    }

    private void handleUpdateSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        String title = parts[2];
        String artist = parts[3];
        String album = parts[4];
        String year = parts[5];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Song song = session.get(Song.class, title);
            if (song != null) {
                song.setArtist(artist);
                song.setAlbum(album);
                song.setReleaseYear(year);
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
        Integer Id = parseInt(parts[2]);

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Song song = session.get(Song.class, Id);
            if (song != null) {
                System.out.println("Deleting song: " + song.getId() + " - " + song.getTitle());
                session.delete(song);
                session.getTransaction().commit();

                writer.println("DELETE_SONG_OK");
            } else {
                writer.println("DELETE_SONG_FAIL");
            }
        } else if (userInfo == null){
            writer.println("DELETE_SONG_FAIL|INVALID_SESSION_ID");
        } else {
            writer.println("DELETE_SONG_FAIL");
        }
    }

    private int generateSongId() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("SELECT MAX(id) FROM Song");
        int maxId = (int) query.getSingleResult();
        session.getTransaction().commit();
        return maxId + 1;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}