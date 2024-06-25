package com.javafx.songmanager.controllers;

import com.javafx.songmanager.models.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserAppController {
    private String clientSessionId;
    private String username;

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
    private TableColumn<Song, Integer> viewYearCol;
    @FXML
    private TextField searchBar;

    @FXML
    void initialize(){
        viewIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        viewTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        viewAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        viewArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        viewYearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        refreshTable();
    }

    private void refreshTable() {
        requestSongList();
        // Clear the table and repopulate it with the updated list of songs
        viewTable.getItems().clear();

    }

    public UserAppController() {
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void exitButtonOnAction(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void chatButtonOnAction(ActionEvent actionEvent) {
        // open chat window

    }
    public void requestSongList() {
        System.out.println("Requesting songs from the server...");
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("RETRIEVE_SONGS " + clientSessionId);

            String response = in.readLine();
            System.out.println("Server response: " + response);

            if ("RETRIEVE_SONGS_FAIL".equals(response)) {
                System.out.println("Server failed to retrieve songs");
                promptAlert("Retrieve Songs Failed", "Failed to retrieve songs from the server");
            } else {
                System.out.println("Server returning songs... Printing server response: ");
                // split the response
                String[] parts = response.split(" ");
                ObservableList<Song> songs = FXCollections.observableArrayList();
                int i = 1;
                while (!parts[i].equals("EOL")){
                    System.out.println(parts[i] + " " + parts[i+1] + " " + parts[i+2] + " " + parts[i+3] + " " + parts[i+4]);
                    Song song = new Song(Integer.parseInt(parts[i]), parts[i+1], parts[i+2], parts[i+3], parts[i+4]);
                    songs.add(song);
                    i += 5;
                }
                viewTable.setItems(songs);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void promptAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
