package com.javafx.songmanager;

import com.javafx.songmanager.utils.LaunchGUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LaunchGUI.launch(primaryStage, "/com/javafx/songmanager/views/login-view.fxml", "Login");
    }
}