package com.javafx.songmanager.models;

import com.javafx.songmanager.utils.LaunchGUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientModel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LaunchGUI.launch(primaryStage, "com/javafx/songmanager/views/client-view.fxml", "Login to existing account");
    }
}