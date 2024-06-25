package com.javafx.songmanager.controllers;

import com.javafx.songmanager.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
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

public class LoginController {

    @FXML
    private PasswordField passwordTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private Tooltip passwordShower = null;

    @FXML
    void initialize() {
    }

    @FXML
    void loginOnAction(ActionEvent event) throws IOException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        if (validateLogin(username, password)) {
            requestLogin(username, password);
        }
    }

    @FXML
    boolean validateLogin(String username, String password){
        System.out.println("Validating credentials locally...");

        if (!Validator.isValidUsername(username)) {
            System.out.println("Invalid username");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login unsuccessful");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username");
            alert.showAndWait();
            return false;
        } else if (!Validator.isValidPassword(password)) {
            System.out.println("Invalid password");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login unsuccessful");
            alert.setHeaderText(null);
            alert.setContentText("Invalid password");
            alert.showAndWait();
            return false;
        }

        return true;
    };

    void requestLogin(String username, String password) {
        System.out.println("Requesting login request to the server...");

        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("LOGIN|" + username + "|" + password);

            String response = in.readLine();
            // split the response
            String[] parts = response.split("\\|");

            System.out.println("Server response: " + response);
            if (parts[0].equals("LOGIN_OK")) {
                System.out.println("User login successful");
                System.out.println("User session id: " + parts[1]);
                Stage stage = (Stage) usernameTextField.getScene().getWindow();
                switchToUserApp(stage, parts[1]);
            } else if (parts[0].equals("LOGIN_OK_ADMIN")) {
                System.out.println("Admin login successful");
                System.out.println("Admin session id: " + parts[1]);
                Stage stage = (Stage) usernameTextField.getScene().getWindow();
                switchToAdminApp(stage, parts[1]);
            } else if ("LOGIN_FAILED|USER_NON_EXIST".equals(response)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("User does not exist! Do you want to register?");
                alert.showAndWait();
                if (alert.getResult().getText().equals("OK")) {
                    switchToRegisterOnAction(new ActionEvent());
                }
            } else if ("LOGIN_FAILED|PASSWORD_INCORRECT".equals(response)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Wrong password! Please try again.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("User login failed!"+ '\n' + "Server response: " + response);
                alert.showAndWait();
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void switchToUserApp(Stage stage, String sessionId) {
        // Load the user app view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/user-app-view.fxml"));
            UserAppController controller = new UserAppController();
            controller.setClientSessionId(sessionId);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void switchToAdminApp(Stage stage, String sessionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/admin-app-view.fxml"));
            AdminAppController controller = new AdminAppController();
            controller.setClientSessionId(sessionId);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void switchToRegisterOnAction(ActionEvent event) {
        // Get the current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Load the register view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javafx/songmanager/views/register-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showPassword(ActionEvent event) {
        if (passwordShower != null) {
            passwordShower.hide();
        }

        Tooltip passwordToolTip = new Tooltip();
        passwordToolTip.setText(passwordTextField.getText());

        Point2D point = passwordTextField.localToScreen(0, 0);
        double x = point.getX();
        double y = point.getY() + passwordTextField.getHeight();
        passwordToolTip.show(passwordTextField, x, y);
        passwordShower = passwordToolTip; // Add this line

        passwordTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                passwordToolTip.hide();
            }
        });
    }

}