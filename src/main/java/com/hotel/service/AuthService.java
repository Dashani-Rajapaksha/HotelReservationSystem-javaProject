package com.hotel.service;

import com.hotel.dao.UserDAO;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password) {

        if (username == null || username.isBlank()) {
            System.out.println("Username cannot be empty.");
            return false;
        }

        if (password == null || password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return false;
        }

        boolean authenticated = userDAO.authenticate(username, password);

        if (!authenticated) {
            System.out.println("Invalid username or password.");
            return false;
        }

        System.out.println("Login successful!");
        return true;
    }
}