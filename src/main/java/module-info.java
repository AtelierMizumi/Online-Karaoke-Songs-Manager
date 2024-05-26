module com.javafx.chatapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires java.sql;

    requires java.persistence;
    requires org.kordamp.ikonli.core;
    requires org.hibernate.orm.core;
    requires hibernate.entitymanager;
    requires org.hibernate.commons.annotations;
    requires java.naming;
    requires jbcrypt;
    requires com.h2database;
    requires jjwt.api;
    requires jjwt.impl;
    requires jjwt.jackson;

    opens com.javafx.songmanager to javafx.fxml;
    opens com.javafx.songmanager.models to org.hibernate.orm.core;
    exports com.javafx.songmanager;
    exports com.javafx.songmanager.utils;
    opens com.javafx.songmanager.utils to javafx.fxml;
    exports com.javafx.songmanager.controllers;
    opens com.javafx.songmanager.controllers to javafx.fxml;
    exports com.javafx.songmanager.models;
}