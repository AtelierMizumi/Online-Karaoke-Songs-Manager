package com.javafx.songmanager.controllers;

import com.javafx.songmanager.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterController {
    @FXML
    private PasswordField confirmPasswordTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField userEmailTextField;

    @FXML
    void initialize() {
    }

    @FXML
    void registerOnAction(ActionEvent event) {
        if (validateRegister(event)) {
            String email = userEmailTextField.getText();
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            requestAccountCreation(email, username, password);
        }
    }

    @FXML
    void switchToLoginOnAction(ActionEvent event) {
        // Get the current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Load the register view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    boolean validateRegister(ActionEvent event) {
        System.out.println("Validating registration...");
        String email = userEmailTextField.getText();
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();

        if (!Validator.isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return false;
        } else if (!Validator.isValidUsername(username)) {
            showAlert("Invalid Username", "Username must be at least 3 characters and can only contain alphanumeric characters, underscores, and hyphens.");
            return false;
        } else if (!Validator.isValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 8 characters and include at least one digit, one lowercase letter, one uppercase letter, and one special character.");
            return false;
        } else if (!Validator.isValidConfirmPassword(password, confirmPassword)) {
            showAlert("Invalid Password", "Passwords do not match.");
            return false;
        } else {
            System.out.println("No problems with credentials");
            return true;
        }
    }
    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void requestAccountCreation(String email, String username, String password) {
        System.out.println("Requesting account creation to the server...");
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("REGISTER " + username + " " + password + " " + email);

            String response = in.readLine();
            System.out.println("Server response: " + response);
            if ("REGISTER_OK".equals(response)) {
                // Registration successful
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("User registered successfully!");
                alert.showAndWait();

                switchToApp();
            } else {
                // Registration failed
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Registration Failed");
                alert.setHeaderText(null);
                alert.setContentText("User registration failed!");
                alert.showAndWait();
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showPassword(ActionEvent event) {
        String password = passwordTextField.getText();
        Tooltip tooltip = new Tooltip(password);
        Tooltip.install(passwordTextField, tooltip);

        passwordTextField.setOnMouseExited(e -> tooltip.hide());
    }

    @FXML
    void switchToApp() {
        // Close the current stage
        Stage stage = (Stage) usernameTextField.getScene().getWindow();
        stage.close();
        // Open the app stage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/app-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
