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
    // =====================================================
    // Get Room ID by Reservation ID (ACTIVE only)
    // =====================================================
    public int getRoomIdByReservationId(Connection conn, int reservationId) {

        String sql = "SELECT room_id FROM reservations WHERE reservation_id = ? AND status = 'ACTIVE'";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reservationId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("room_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // =====================================================
    // Update Reservation Status
    // =====================================================
    public boolean updateReservationStatus(Connection conn, int reservationId, String status) {

        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public Reservation findById(Connection conn, int reservationId) {

    String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, reservationId);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Reservation r = new Reservation();
            r.setReservationId(rs.getInt("reservation_id"));
            r.setGuestId(rs.getInt("guest_id"));
            r.setRoomId(rs.getInt("room_id"));
            r.setCheckIn(rs.getString("check_in"));
            r.setCheckOut(rs.getString("check_out"));
            return r;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
}