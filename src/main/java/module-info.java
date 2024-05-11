module com.javafx.chatapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires java.sql;

    opens com.javafx.songmanager to javafx.fxml;
    exports com.javafx.songmanager;
    exports com.javafx.songmanager.utils;
    opens com.javafx.songmanager.utils to javafx.fxml;
    exports com.javafx.songmanager.controllers;
    opens com.javafx.songmanager.controllers to javafx.fxml;
    exports com.javafx.songmanager.models;
    opens com.javafx.songmanager.models to javafx.fxml;
}