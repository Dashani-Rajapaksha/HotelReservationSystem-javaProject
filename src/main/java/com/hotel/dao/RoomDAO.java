package com.hotel.dao;

import com.hotel.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoomDAO {

    public int findAvailableRoom(int typeId, String checkIn, String checkOut) {

        String sql =
        "SELECT r.room_id " +
        "FROM room r " +
        "WHERE r.type_id = ? " +
        "AND r.room_id NOT IN ( " +
        "SELECT room_id FROM reservations " +
        "WHERE (check_in < ? AND check_out > ?) " +
        ") LIMIT 1";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, typeId);
            stmt.setString(2, checkOut);
            stmt.setString(3, checkIn);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("room_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // no room available
    }
}