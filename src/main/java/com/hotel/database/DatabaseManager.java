package com.hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/hotel_db",
                    "root",
                    ""
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}