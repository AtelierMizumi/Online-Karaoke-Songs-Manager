package com.javafx.songmanager.controllers;

import com.javafx.songmanager.utils.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class LoginController {

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    void initialize() {
        DatabaseHandler databaseHandler = new DatabaseHandler();
    }

    @FXML
    void loginOnAction(ActionEvent event) {

    }

    @FXML
    void switchToLoginOnAction(ActionEvent event) {

    }

    @FXML
    void showPassword(ActionEvent event) {
        String password = passwordTextField.getText();
        Tooltip tooltip = new Tooltip(password);
        Tooltip.install(passwordTextField, tooltip);

        passwordTextField.setOnMouseMoved(e -> tooltip.hide());
    }

}