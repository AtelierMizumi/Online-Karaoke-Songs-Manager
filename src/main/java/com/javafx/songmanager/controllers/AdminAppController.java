package com.javafx.songmanager.controllers;

import com.javafx.songmanager.models.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;

import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;

import java.awt.Desktop;
import java.util.UUID;

public class AdminAppController {
    private final String cacheDirectory = "clientCache";
    private String clientSessionId;
    private ObservableList<Song> SONGLIST = FXCollections.observableArrayList();

    // Table View
    @FXML
    private TableView<Song> viewTable;
    @FXML
    private TableColumn<Song, Integer> viewIDCol;
    @FXML
    private TableColumn<Song, String> viewTitleCol;
    @FXML
    private TableColumn<Song, String> viewArtistCol;
    @FXML
    private TableColumn<Song, String> viewAlbumCol;
    @FXML
    private TableColumn<Song, String> viewYearCol;

    @FXML
    private TableView<Song> previewAddTable;
    @FXML
    private TableColumn<Song, Integer> addIDCol;
    @FXML
    private TableColumn<Song, String> addTitleCol;
    @FXML
    private TableColumn<Song, String> addArtistCol;
    @FXML
    private TableColumn<Song, String> addAlbumCol;
    @FXML
    private TableColumn<Song, String> addYearCol;


    // Delete Table
    @FXML
    private TableView<Song> previewDeleteTable;
    @FXML
    private TableColumn<Song, Integer> deleteIDCol;
    @FXML
    private TableColumn<Song, String> deleteTitleCol;
    @FXML
    private TableColumn<Song, String> deleteAlbumCol;
    @FXML
    private TableColumn<Song, String> deleteArtistCol;
    @FXML
    private TableColumn<Song, String> deleteYearCol;
    // Edit Table
    @FXML
    private TableView<Song> previewEditTable;
    @FXML
    private TableColumn<Song, String> editArtistCol;
    @FXML
    private TableColumn<Song, Integer> editIDCol;
    @FXML
    private TableColumn<Song, String> editTitleCol;
    @FXML
    private TableColumn<Song, String> editAlbumCol;
    @FXML
    private TableColumn<Song, String> editYearCol;

    // TextField
    @FXML
    private TextField addTabTitleTextField;
    @FXML
    private TextField addTabAlbumTextField;
    @FXML
    private TextField addTabArtistTextField;
    @FXML
    private TextField addTabYearTextField;
    @FXML
    private TextField addTabAudioPath;
    @FXML
    private TextField editTitleTextField;
    @FXML
    private TextField editAlbumTextField;
    @FXML
    private TextField editArtistTextField;
    @FXML
    private TextField editSongPathTextField;
    @FXML
    private TextField editYearTextField;
    @FXML
    private TextField searchBar;
    @FXML
    private TextField editSearchBar;
    @FXML
    private TextField deleteSearchBar;

    // Button
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button editButton;
    @FXML
    private Button viewButton;

    // AnchorPane
    @FXML
    private AnchorPane addTab;
    @FXML
    private AnchorPane deleteTab;
    @FXML
    private AnchorPane editTab;
    @FXML
    private AnchorPane viewTab;

    public void initialize() throws IOException {
        System.out.println("Initializing AdminAppController...");
        System.out.println("SessionId:" + clientSessionId);
        requestSongList(clientSessionId);

        viewIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        viewTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        viewAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        viewArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        viewYearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));

        viewTable.setItems(SONGLIST);

        viewTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Song selectedSong = viewTable.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    try {
                        playMedia(selectedSong);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Create a FilteredList wrapping the ObservableList
        FilteredList<Song> viewTableFilteredList = new FilteredList<>(SONGLIST, p -> true);

        // Set the filter Predicate whenever the filter changes
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            viewTableFilteredList.setPredicate(song -> {
                // If filter text is empty, display all songs
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare song title, album, and artist with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                return song.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        song.getAlbum().toLowerCase().contains(lowerCaseFilter) ||
                        song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Wrap the FilteredList in a SortedList
        SortedList<Song> viewTableSortedList = new SortedList<>(viewTableFilteredList);
        // Initialize viewTable with the sorted (and filtered) data
        viewTable.setItems(viewTableSortedList);

        // Initialize addTable
        addIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        addTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        addArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        addAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        addYearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));

        previewAddTable.setItems(SONGLIST);
        // Create a FilteredList for addTable
        FilteredList<Song> addTabFilteredList = new FilteredList<>(SONGLIST, p -> true);

        // Init deleteTable
        deleteIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        deleteTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        deleteArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        deleteAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        deleteYearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));

        previewDeleteTable.setItems(SONGLIST);
        // Create a FilteredList for deleteTable
        FilteredList<Song> deleteTabFilteredList = new FilteredList<>(SONGLIST, p -> true);

        // Set the filter Predicate whenever the filter changes
        deleteSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteTabFilteredList.setPredicate(song -> {
                // If filter text is empty, display all songs
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare song title, album, and artist with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                return song.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        song.getAlbum().toLowerCase().contains(lowerCaseFilter) ||
                        song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Wrap the FilteredList in a SortedList
        SortedList<Song> deleteSortedList = new SortedList<>(deleteTabFilteredList);
        // Initialize viewTable with the sorted (and filtered) data
        previewDeleteTable.setItems(deleteSortedList);
        // Allow multiple selections in the deleteTable
        previewDeleteTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Init editTable
        editIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        editTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        editArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        editAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        editYearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));

        // Create a FilteredList for editTable
        FilteredList<Song> editTabFilteredList = new FilteredList<>(SONGLIST, p -> true);

        editSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            editTabFilteredList.setPredicate(song -> {
                // If filter text is empty, display all songs
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare song title, album, and artist with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                return song.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        song.getAlbum().toLowerCase().contains(lowerCaseFilter) ||
                        song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        previewEditTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            System.out.println("Listener triggered"); // Debugging line
            if (newSelection != null) {
                System.out.println("New selection is not null"); // Debugging line
                Song selectedSong = previewEditTable.getSelectionModel().getSelectedItem();
                System.out.println("Selected song: " + selectedSong); // Debugging line
                editTitleTextField.setText(selectedSong.getTitle());
                editArtistTextField.setText(selectedSong.getArtist());
                editAlbumTextField.setText(selectedSong.getAlbum());
                editYearTextField.setText(selectedSong.getReleaseYear());
            } else {
                System.out.println("New selection is null"); // Debugging line
            }
        });

        // Wrap the FilteredList in a SortedList
        SortedList<Song> editSortedList = new SortedList<>(editTabFilteredList);
        // Initialize viewTable with the sorted (and filtered) data
        previewEditTable.setItems(editSortedList);


        viewTableSortedList.comparatorProperty().bind(viewTable.comparatorProperty());
        deleteSortedList.comparatorProperty().bind(previewDeleteTable.comparatorProperty());

        System.out.println("All tables initialized. Current session id: " + clientSessionId);
    }

    private void playMedia(Song selectedSong) throws IOException {
        String directoryName = cacheDirectory + "/Songs";
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

        Socket clientSocket = new Socket("localhost", 8080);
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer.println("GET_MEDIA|" + clientSessionId + "|" + selectedSong.getId());


        String[] parts = reader.readLine().split("\\|", -1);
        System.out.println("Server response: " + parts[0] + " " + parts[1] + " " + parts[2]);
        if ("GET_MEDIA_FAIL".equals(parts[0])) {
            System.out.println("Failed to get media");
        } else if ("NO_MEDIA_FOUND".equals(parts[0])) {
            System.out.println("No media found");
        } else {
            String songName = parts[1];
            String fileExtension = parts[2];

            String newFilePath = songName + "." + fileExtension;
            DataInputStream din = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());

            writer.println("LISTENING_FOR_MEDIA");

            try {
                int bytes = 0;
                FileOutputStream fileOutputStream
                        = new FileOutputStream(directoryName + "/" + newFilePath);

                long size
                        = din.readLong(); // read file size
                System.out.println("File size: " + size);
                byte[] buffer = new byte[4 * 1024];
                while (size > 0
                        && (bytes = din.read(
                        buffer, 0,
                        (int)Math.min(buffer.length, size)))
                        != -1) {
                    // Here we write the file using write method
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes; // read upto file size
                }
                // Here we received file
                System.out.println("File is Received");
                fileOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Media uploaded successfully");
            din.close();
            dout.close();
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    System.out.println("Playing: " + directoryName + "/" + newFilePath);
                    Desktop.getDesktop().open(new File(cacheDirectory + "/" + "Songs" + "/" + newFilePath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (SystemUtils.IS_OS_MAC) {
                System.out.println("Playing: " + directoryName + "/" + newFilePath);
                ProcessBuilder pb = new ProcessBuilder("open", cacheDirectory + "/" + "Songs" + "/" + newFilePath);
                try {
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (SystemUtils.IS_OS_LINUX){
                System.out.println("Playing: " + directoryName + "/" + newFilePath + " with xdg-open");
                ProcessBuilder pb = new ProcessBuilder("xdg-open", cacheDirectory + "/" + "Songs" + "/" + newFilePath);
                try {
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void editTabEditButtonOnAction(ActionEvent event) {
        try {
            String title = editTitleTextField.getText().trim();
            String album = editAlbumTextField.getText().trim();
            String artist = editArtistTextField.getText().trim();
            String year = editYearTextField.getText().trim();
            String audioFilePath = editSongPathTextField.getText().trim();
            // get selected song
            Song selectedSong = previewEditTable.getSelectionModel().getSelectedItem();
            if (selectedSong == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a song to edit.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            // Validate title
            if (title == null || title.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Title cannot be empty.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            // request song creation to the server
            try {
                Socket socket = new Socket("localhost", 8080);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("UPDATE_SONG|" + clientSessionId + "|" + selectedSong.getId()+ "|" + title + "|" + artist + "|" + album + "|" + year);

                String response = in.readLine();
                System.out.println("Received response: " + response);

                String[] parts = response.split("\\|", -1);
                if (parts[0].equals("UPDATE_SONG_OK")) {
                    System.out.println("Song updated successfully. Uploading media...");
                    requestSongList(clientSessionId);
                    if (!audioFilePath.isEmpty()) {
                        uploadSongMedia(selectedSong.getId(), audioFilePath);
                    } else {
                        System.out.println("No audio file selected. Skipping media upload.");
                    }
                } else if ("UPDATE_SONG_FAIL|INVALID_SESSION".equals(response)) {
                    System.out.println("Invalid session id");
                } else {
                    System.out.println("Failed to update song");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void addTabUploadButtonOnAction(ActionEvent event) {
        try {
            String title = addTabTitleTextField.getText().trim();
            String album = addTabAlbumTextField.getText().trim();
            String artist = addTabArtistTextField.getText().trim();
            String year = addTabYearTextField.getText().trim();
            String audioFilePath = addTabAudioPath.getText().trim();

            // Validate title
            if (title == null || title.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Title cannot be empty.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            // request song creation to the server
                try {
                    Socket socket = new Socket("localhost", 8080);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("CREATE_SONG|" + clientSessionId + "|" + title + "|" + artist + "|" + album + "|" + year);

                    String response = in.readLine();
                    System.out.println("Received response: " + response);

                    String[] parts = response.split("\\|", -1);
                    if (parts[0].equals("CREATE_SONG_OK")) {
                        System.out.println("Song created successfully. Uploading media...");
                        requestSongList(clientSessionId);
                            if (!audioFilePath.isEmpty()) {
                                uploadSongMedia(Integer.parseInt(parts[1]), audioFilePath);
                            } else {
                                System.out.println("No audio file selected. Skipping media upload.");
                            }
                        } else if ("CREATE_SONG_OK".equals(response)) {
                        System.out.println("No audio file selected");
                    } else {
                        System.out.println("Failed to create song");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    void editTabUploadAudioButtonOnAction(ActionEvent event) {
        getMediaPath(editSongPathTextField);
    }

    private String getAudioFromFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files", "*.wav", "*.mp3", "*.aac", "*.ogg", "*.flac", "*.mp4", "*.avi", "*.mkv")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            System.out.println("No file selected");
            return null;
        }
    }
    public void uploadSongMedia(int songId, String mediaFilePath) throws IOException {
        Socket socket = new Socket("localhost", 8080);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String fileExtension = getFileExtension(mediaFilePath);
        out.println("ADD_SONG_MEDIA_TO_ID|" + clientSessionId + "|" + songId + "|" + fileExtension);

        String response = in.readLine();
        System.out.println("Received response: " + response);
        if ("LISTENING_FOR_MEDIA".equals(response)) {
            System.out.println("Server is ready to receive media. Sending...");

            try {
                DataInputStream din = new DataInputStream(socket.getInputStream());
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                System.out.println("Sending media file: " + mediaFilePath);
                int bytes = 0;
                File file = new File(mediaFilePath);
                FileInputStream fileInputStream = new FileInputStream(file);

                dout.writeLong(file.length());
                // Here we  break file into chunks
                byte[] buffer = new byte[4 * 1024];
                while ((bytes = fileInputStream.read(buffer))
                        != -1) {
                    dout.write(buffer, 0, bytes);
                    dout.flush();
                }
                fileInputStream.close();

                response = in.readLine();
                dout.close();
                din.close();

                if ("UPLOAD_SONG_MEDIA_SUCCESS".equals(response)) {
                    System.out.println("Media uploaded successfully");
                } else {
                    System.out.println("Failed to upload media");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Failed to upload media");
            return;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    @FXML
    void exitButtonOnAction(ActionEvent event) {
        System.exit(0);
    }
    @FXML
    void editTabClearButtonOnAction(ActionEvent event){
        editTitleTextField.clear();
        editArtistTextField.clear();
        editAlbumTextField.clear();
        editSongPathTextField.clear();
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == viewButton) {
            viewTab.setVisible(true);
            addTab.setVisible(false);
            editTab.setVisible(false);
            deleteTab.setVisible(false);
        } else if (event.getSource() == addButton) {
            viewTab.setVisible(false);
            addTab.setVisible(true);
            editTab.setVisible(false);
            deleteTab.setVisible(false);
        } else if (event.getSource() == editButton) {
            viewTab.setVisible(false);
            addTab.setVisible(false);
            editTab.setVisible(true);
            deleteTab.setVisible(false);
        } else if (event.getSource() == deleteButton) {
            viewTab.setVisible(false);
            addTab.setVisible(false);
            editTab.setVisible(false);
            deleteTab.setVisible(true);
        }
    }
    @FXML
    void deleteTabDeleteButtonOnAction(ActionEvent event) {
        // Get all selected songs from the deleteTable
        ObservableList<Song> selectedSongs = previewDeleteTable.getSelectionModel().getSelectedItems();

        if (selectedSongs != null && !selectedSongs.isEmpty()) {
            for (Song song : selectedSongs) {
                try {
                    Socket socket = new Socket("localhost", 8080);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("DELETE_SONG|" + clientSessionId + "|" + song.getId());

                    String response = in.readLine();
                    if ("DELETE_SONG_SUCCESS".equals(response)) {
                        System.out.println("Song with id " + song.getId() + " deleted successfully");
                    } else {
                        System.out.println("Failed to delete song");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            requestSongList(clientSessionId);
        }
    }

        private void getMediaPath (TextField AudioPath){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Media Files", "*.wav", "*.mp3", "*.aac", "*.ogg", "*.flac", "*.mp4", "*.avi", "*.mkv")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                String audioFilePath = selectedFile.getAbsolutePath();
                AudioPath.setText(audioFilePath);
            } else {
                System.out.println("No file selected");
            }
        }
    public ObservableList<Song> requestSongList(String clientSessionId) {
        System.out.println("Requesting songs from the server...");
        ObservableList<Song> songs = FXCollections.observableArrayList();
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("RETRIEVE_SONGS|" + clientSessionId);

            String response;
            do {
                response = in.readLine();
                if (response != null) {
                    if ("RETRIEVE_SONGS_FAIL".equals(response)) {
                        System.out.println("Server failed to retrieve songs");
                        break;
                    } else if ("END_SONGS".equals(response)) {
                        System.out.println("End of song list");
                        break;
                    } else if ("RETRIEVE_SONGS_FAIL|NULL_SESSION_ID".equals(response)) {
                        System.out.println("Server failed to retrieve songs due to null session id");
                        break;
                    } else {
                        System.out.println("Server response: " + response);
                        // split the response
                        String[] parts = response.split("\\|", -1);
                        Song song = new Song(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3], parts[4]);
                        songs.add(song);
                    }
                }
            } while (response != null && !response.equals("END_SONGS"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SONGLIST.clear();
        SONGLIST.addAll(songs);
        return songs;
    }

    private void promptAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String sessionId) {
        this.clientSessionId = sessionId;
    }

    public void chatButtonOnAction(ActionEvent actionEvent) {
        // Open the chat window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/chat-window-view.fxml"));
            ChatController chatController = new ChatController();
            chatController.setClientSessionId(clientSessionId);

            loader.setController(chatController);

            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTabUploadAudioButtonOnAction(ActionEvent actionEvent) {
        getMediaPath(addTabAudioPath);
    }

    public void addTabClearButton(ActionEvent actionEvent) {
        addTabTitleTextField.clear();
        addTabAlbumTextField.clear();
        addTabArtistTextField.clear();
        addTabYearTextField.clear();
        addTabAudioPath.clear();
    }
}

