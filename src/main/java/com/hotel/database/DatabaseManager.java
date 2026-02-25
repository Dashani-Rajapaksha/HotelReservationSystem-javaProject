package com.hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private final String USER = "root";
    private final String PASSWORD = "";

    private DatabaseManager() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() throws ClassNotFoundException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    

    public Connection getConnection() {
        return connection;
    }
}