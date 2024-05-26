package com.javafx.songmanager.utils;

public class Validator {
    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }

    public static boolean isValidUsername(String username) {
        String regex = "^[a-zA-Z0-9_-]{3,}$";
        return username.matches(regex);
    }

    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,}$";
        return password.matches(regex);
    }

    public static boolean isValidConfirmPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}