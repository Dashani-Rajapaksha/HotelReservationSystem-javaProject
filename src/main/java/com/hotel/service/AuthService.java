package com.hotel.service;

import com.hotel.dao.UserDAO;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password) {

        if (username == null || username.isBlank()) {
            System.out.println("Username empty");
            return false;
        }

        if (password == null || password.isBlank()) {
            System.out.println("Password empty");
            return false;
        }

        return userDAO.authenticate(username, password);
    }
}