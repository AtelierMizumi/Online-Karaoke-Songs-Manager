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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.parseInt;

public class SongManagerServer {
    private static final String PERSISTENCE_UNIT_NAME = "databasePU";
    private final String songFolder = "Songs";
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
                    case "GET_MEDIA" -> handleGetMedia(parts, clientSocket, writer);
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

    private void handleGetMedia(String[] parts, Socket clientSocket, PrintWriter writer) {
        String sessionId = parts[1];
        int songId = parseInt(parts[2]);

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM SongPath WHERE id = :songId");
            query.setParameter("songId", songId);
            List<SongPath> results = query.getResultList();
            session.getTransaction().commit();

            if (!results.isEmpty()) {
                SongPath path = results.get(0);


            if (path != null) {
                String songName = path.getPath();
                File file = new File(songFolder + "/" + songName);
                String extension = songName.substring(songName.lastIndexOf(".") + 1);
                String name = songName.substring(0, songName.lastIndexOf("."));
                writer.println("GET_MEDIA_OK|" + name + "|" + extension);
                if (file.exists()) {
                    try {
                        DataInputStream din = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                        System.out.println("Sending media file: " + songName);

                        int bytes = 0;
                        // Open the File where he located in your pc
                        FileInputStream fileInputStream
                                = new FileInputStream(file);

                        dout.writeLong(file.length());
                        byte[] buffer = new byte[4 * 1024];
                        while ((bytes = fileInputStream.read(buffer))
                                != -1) {
                            dout.write(buffer, 0, bytes);
                            dout.flush();
                        }
                        // close the file here
                        fileInputStream.close();
                        writer.println("UPLOAD_SONG_MEDIA_SUCCESS");
                        writer.println("GET_MEDIA_OK");
                    } catch (IOException e) {
                        writer.println("GET_MEDIA_FAIL");
                        throw new RuntimeException(e);
                    }
                } else if (!file.exists()){
                    writer.println("NO_MEDIA_FOUND");
                }
            } else {
                writer.println("GET_MEDIA_FAIL");
            }}
        } else {
            writer.println("GET_MEDIA_FAIL|INVALID_SESSION_ID");
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
    private void handleAddSongMediaToId(String[] parts, Socket clientSocket, PrintWriter writer) throws IOException {
        String sessionId = parts[1];
        int songId = parseInt(parts[2]);
        String fileExtension = parts[3];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            DataInputStream din = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
            String directoryName = songFolder;
            File directory = new File(directoryName);
            if (!directory.exists()) {
                // Create the directory
                boolean isCreated = directory.mkdir();
                if (isCreated) {
                    System.out.println("Directory created successfully.");
                } else {
                    System.out.println("Failed to create the directory.");
                }
            } else { System.out.println("Directory already exists."); }
            String newFilePath = UUID.randomUUID().toString() + "." + fileExtension;

            // Delete the old song file if it exists
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM SongPath WHERE id = :songId");
            query.setParameter("songId", songId);
            List<SongPath> results = query.getResultList();
            if (!results.isEmpty()) {
                SongPath oldSongPath = results.get(0);
                File oldFile = new File(songFolder + "/" + oldSongPath.getPath());
                if (oldFile.exists()) {
                    oldFile.delete();
                    session.delete(oldSongPath);
                }
            }
            session.getTransaction().commit();

            writer.println("LISTENING_FOR_MEDIA");
            // Listen for the file
            try {
                int bytes = 0;
                FileOutputStream fileOutputStream = new FileOutputStream(songFolder + "/" + newFilePath);
                long size = din.readLong();
                System.out.println("File size: " + size);
                byte[] buffer = new byte[4 * 1024];
                while (size > 0
                        && (bytes = din.read(
                        buffer, 0,
                        (int) Math.min(buffer.length, size)))
                        != -1) {
                    // Here we write the file using write method
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes; // read upto file size
                }

                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            writer.println("UPLOAD_SONG_MEDIA_SUCCESS");
            System.out.println("Media uploaded successfully");
            din.close();
            dout.close();

            // Save the path to the database
            session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            SongPath songPath = new SongPath(songId, newFilePath);
            session.save(songPath);
            session.getTransaction().commit();
        } else {
            writer.println("UPLOAD_SONG_MEDIA_FAIL");
        }
    }

    private String newHashedName(String fileExtension) {
        return UUID.randomUUID().toString() + "." + fileExtension;
    }

    private UserInfo getSessionUserInfo(String sessionId) {
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
        int songId = Integer.parseInt(parts[2]);
        String title = parts[3];
        String artist = parts[4];
        String album = parts[5];
        String year = parts[6];

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Song song = session.get(Song.class, songId);
            if (song != null) {
                song.setTitle(title);
                song.setArtist(artist);
                song.setAlbum(album);
                song.setReleaseYear(year);
                session.update(song);
                session.getTransaction().commit();

                writer.println("UPDATE_SONG_OK");
            } else {
                writer.println("UPDATE_SONG_FAIL|SONG_NOT_FOUND");
            }
        } else {
            writer.println("UPDATE_SONG_FAIL|INVALID_SESSION_ID");
        }
    }

    private void handleDeleteSong(String[] parts, PrintWriter writer) {
        String sessionId = parts[1];
        Integer Id = parseInt(parts[2]);

        UserInfo userInfo = getSessionUserInfo(sessionId);
        if (userInfo != null) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            SongPath songPath = session.get(SongPath.class, Id);
            if (songPath == null) {
                System.out.println("SongPath object non existent.");
            } else {
                Path path = Paths.get(songFolder + "/" + songPath.getPath());
                if (Files.exists(path)) {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Deleting Path: " + songPath.getId() + " - " + songPath.getPath());
                // Ensure that songPath is managed by the current session
                if (!session.contains(songPath)) {
                    songPath = (SongPath) session.merge(songPath);
                }
                session.delete(songPath);
            }

            Song song = session.get(Song.class, Id);
            if (song != null) {
                System.out.println("Deleting song: " + song.getId() + " - " + song.getTitle());
                // Ensure that song is managed by the current session
                if (!session.contains(song)) {
                    song = (Song) session.merge(song);
                }
                session.delete(song);
                writer.println("DELETE_SONG_OK");
            } else {
                writer.println("DELETE_SONG_FAIL");
            }
            session.getTransaction().commit();
        } else {
            writer.println("DELETE_SONG_FAIL|INVALID_SESSION_ID");
        }
    }

    private int generateSongId() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("SELECT MAX(id) FROM Song");
        if (query.getSingleResult() == null) {
            session.getTransaction().commit();
            session.close();
            return 1;
        }
        int maxId = (int) query.getSingleResult();
        session.getTransaction().commit();
        session.close();
        return maxId + 1;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}