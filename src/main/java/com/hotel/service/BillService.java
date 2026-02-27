package com.hotel.service;

import com.hotel.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillService {

    public double calculateTotal(Connection conn, int reservationId){

        String sql =
                "SELECT rt.rate, r.check_in, r.check_out " +
                "FROM reservations r " +
                "JOIN room rm ON r.room_id = rm.room_id " +
                "JOIN room_types rt ON rm.type_id = rt.type_id " +
                "WHERE r.reservation_id = ?";

        try {
            

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reservationId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                double rate = rs.getDouble("rate");

                LocalDate checkIn = rs.getDate("check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out").toLocalDate();

                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

                return rate * nights;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
