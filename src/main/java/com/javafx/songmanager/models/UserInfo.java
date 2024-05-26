package com.javafx.songmanager.models;

import javax.persistence.*;

@Entity
@Table(name = "UserInfo")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private int id;

    private String email_Address;

    private String username;

    private String hashed_password;

    private String password_salt;

    private boolean is_admin_user;

    public UserInfo() {    }

    public UserInfo(String email_Address, String username, String hashed_password, String password_salt, boolean is_admin) {
        this.email_Address = email_Address;
        this.username = username;
        this.hashed_password = hashed_password;
        this.password_salt = password_salt;
        this.is_admin_user = is_admin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getemail_Address() {
        return email_Address;
    }

    public void setemail_Address(String email_Address) {
        this.email_Address = email_Address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashed_password() {
        return hashed_password;
    }

    public void setHashed_password(String hashed_password) {
        this.hashed_password = hashed_password;
    }

    public String getPassword_salt() {
        return password_salt;
    }

    public void setPassword_salt(String password_salt) {
        this.password_salt = password_salt;
    }

    public boolean isIs_admin_user() {
        return is_admin_user;
    }

    public void setIs_admin_user(boolean is_admin_user) {
        this.is_admin_user = is_admin_user;
    }
}