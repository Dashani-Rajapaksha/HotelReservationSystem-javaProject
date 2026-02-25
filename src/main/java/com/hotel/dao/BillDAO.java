package com.hotel.dao;

import com.hotel.database.DatabaseManager;
import com.hotel.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class BillDAO {

    public int saveBill(Connection conn, Bill bill) {

        String sql = "INSERT INTO bills (reservation_id, total_amount) VALUES (?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, bill.getReservationId());
            stmt.setDouble(2, bill.getTotalAmount());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // bill_id
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}