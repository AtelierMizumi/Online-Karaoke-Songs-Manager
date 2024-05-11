package com.javafx.songmanager.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class RegisterController {

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    void registerOnAction(ActionEvent event) {

    }

    @FXML
    void switchToRegisterOnAction(ActionEvent event) {

    }

    @FXML
    void showPassword(ActionEvent event) {
        String password = passwordTextField.getText();
        Tooltip tooltip = new Tooltip(password);
        Tooltip.install(passwordTextField, tooltip);

        passwordTextField.setOnMouseMoved(e -> tooltip.hide());
    }

}
