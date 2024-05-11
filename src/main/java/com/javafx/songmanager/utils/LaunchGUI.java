package com.javafx.songmanager.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LaunchGUI {

    public static void launch(Stage primaryStage, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(LaunchGUI.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(LaunchGUI.class.getResourceAsStream("/com/javafx/songmanager/assets/icon.png"))));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}