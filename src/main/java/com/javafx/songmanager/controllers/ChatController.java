package com.javafx.songmanager.controllers;

import com.javafx.songmanager.models.Song;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatController implements Runnable {

    @FXML
    private ListView<String> chatListView;

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendButton;
    @FXML
    private Label usernameLabel;
    private String username;
    private String userEmail;

    private String clientSessionId;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatController(){};

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId;
    }

    @FXML
    void initialize() {
        // Open a socket which listens for incoming messages
        try {
            socket = new Socket("localhost", 8080);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server to request the chat history
            out.println("NEW_CONNECTION " + clientSessionId);

            // Start a new thread to listen for incoming messages
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add a listener to the send button
        sendButton.setOnAction(event -> {
            String message = messageTextField.getText();
            if (!message.isEmpty()) {
                // Add the message to the chatListView
                chatListView.getItems().add(message);
                messageTextField.clear();
            }
        });
    }

    public void sendMessage(ActionEvent actionEvent) {
        String message = messageTextField.getText();
        out.println("SEND_MESSAGE|" + clientSessionId + "|" + message);
    }

    public void receiveMessage(String message) {
        chatListView.getItems().add(message);
    }

    public void getUserCredentials(String clientSessionId){
        // request user's info from the server
        try {
            out.println("GET_USER_INFO|" + clientSessionId);

            String userInfo = in.readLine();
            String[] parts = userInfo.split("\\|");
            if (parts[0].equals("USER_INFO_OK")) {
                this.userEmail = parts[1];
                this.username = parts[2];
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        // listen loop
        while (true) {
            try {
                String message = in.readLine();
                receiveMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}