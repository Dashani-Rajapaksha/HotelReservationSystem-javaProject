package com.hotel.dao;

import com.hotel.database.DatabaseManager;
import com.hotel.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class GuestDAO {

    public int saveOrGetGuest(Connection conn, Guest guest) {

    try {
        // 1️⃣ Check if NIC already exists
        String checkSql = "SELECT guest_id FROM guests WHERE NIC = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, guest.getNic());

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // Guest already exists
            return rs.getInt("guest_id");
        }

        // 2️⃣ Insert new guest
        String insertSql = "INSERT INTO guests (name, address, contact, NIC) VALUES (?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

        insertStmt.setString(1, guest.getName());
        insertStmt.setString(2, guest.getAddress());
        insertStmt.setString(3, guest.getContact());
        insertStmt.setString(4, guest.getNic());

        int rowsInserted = insertStmt.executeUpdate();

        if (rowsInserted > 0) {
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return -1;
}
}