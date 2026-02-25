package com.hotel.dao;

import com.hotel.database.DatabaseManager;
import com.hotel.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReservationDAO {

    public int saveReservation(Connection conn, Reservation reservation) {

        String sql = "INSERT INTO reservations (guest_id, room_id, check_in, check_out) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, reservation.getGuestId());
            stmt.setInt(2, reservation.getRoomId());
            stmt.setString(3, reservation.getCheckIn());
            stmt.setString(4, reservation.getCheckOut());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // reservation_id
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}