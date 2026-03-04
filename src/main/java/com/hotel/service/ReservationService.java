package com.hotel.service;

import com.hotel.dao.*;
import com.hotel.dao.GuestDAO.GuestSaveResult;
import com.hotel.model.*;
import com.hotel.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.hotel.database.DatabaseManager;

public class ReservationService {

    private GuestDAO guestDAO = new GuestDAO();
    private RoomDAO roomDAO = new RoomDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private BillDAO billDAO = new BillDAO();
    private BillService billService = new BillService();

    public BookingResult bookRoom(
            String name,
            String address,
            String contact,
            String nic,
            int roomId,            // changed from typeId
            String checkIn,
            String checkOut
    ) {

        if (!validateInput(name, contact, nic, checkIn, checkOut)) {
            return new BookingResult(false, 0, 0, 0);
        }

        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction (Refactor-friendly)

            // Save or get guest
            Guest guest = new Guest(name, address, contact, nic);
            GuestSaveResult guestResult = guestDAO.saveOrGetGuest(conn, guest);

            if (guestResult == null) {
                throw new Exception("Guest processing failed");
            }

            int guestId = guestResult.getGuestId();
            // Save reservation (room already selected)
            Reservation reservation = new Reservation(
                    guestId,
                    roomId,
                    checkIn,
                    checkOut
            );

            int reservationId = reservationDAO.saveReservation(conn, reservation);

            if (reservationId == -1) {
                throw new Exception("Reservation failed");
            }

            //  Calculate total bill
            double total = billService.calculateTotal(conn, reservationId);

            Bill bill = new Bill(reservationId, total);
            int billId = billDAO.saveBill(conn, bill);

            if (billId == -1) {
                throw new Exception("Bill generation failed");
            }

            //  Update room status to BOOKED
            boolean roomUpdated = roomDAO.updateRoomStatus(conn, roomId, "BOOKED");

            if (!roomUpdated) {
                throw new Exception("Failed to update room status");
            }

            conn.commit(); // Success (Refactor-friendly)

            System.out.println("Booking successful!");
            System.out.println("Reservation ID: " + reservationId);
            System.out.println("Room ID: " + roomId);
            System.out.println("Total: " + total);

            return new BookingResult(true, reservationId, roomId, total);

        } catch (Exception e) {

            try {
                if (conn != null) {
                    conn.rollback(); // Rollback everything (Refactor-friendly)
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }

            System.out.println("Booking failed. Transaction rolled back.");
            e.printStackTrace();

            return new BookingResult(false, 0, 0, 0);

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (Exception ignored) {}
        }
    }
    
    //AVAILABLE ROOMS BUTTON - CHECK OUT PAGE
    public String getAllRoomsJson() {

    StringBuilder json = new StringBuilder();
    json.append("[");

    try {
        Connection conn = DatabaseManager.getInstance().getConnection();

        String sql =
            "SELECT r.room_number, r.status, rt.type_name, rt.rate " +
            "FROM room r " +
            "JOIN room_types rt ON r.type_id = rt.type_id";

        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        boolean first = true;

        while (rs.next()) {

            if (!first) json.append(",");
            first = false;

            json.append("{")
                .append("\"roomNumber\":\"").append(rs.getString("room_number")).append("\",")
                .append("\"typeName\":\"").append(rs.getString("type_name")).append("\",")
                .append("\"rate\":").append(rs.getDouble("rate")).append(",")
                .append("\"status\":\"").append(rs.getString("status")).append("\"")
                .append("}");
        }

        json.append("]");

    } catch (Exception e) {
        e.printStackTrace();
        return "[]";
    }

    return json.toString();
}

    // Validation
    private boolean validateInput(
            String name,
            String contact,
            String nic,
            String checkIn,
            String checkOut
    ) {

        if (name == null || name.isBlank()) {
            System.out.println("Name cannot be empty.");
            return false;
        }

        if (!contact.matches("\\d{10}")) {
            System.out.println("Contact must be 10 digits.");
            return false;
        }

        if (!nic.matches("\\d{12}")) {
            System.out.println("NIC must be 12 digits.");
            return false;
        }

        try {
            java.time.LocalDate inDate = java.time.LocalDate.parse(checkIn);
            java.time.LocalDate outDate = java.time.LocalDate.parse(checkOut);

            if (!outDate.isAfter(inDate)) {
                System.out.println("Check-out must be after check-in.");
                return false;
            }

            if (inDate.isBefore(java.time.LocalDate.now())) {
                System.out.println("Cannot book past dates.");
                return false;
            }

        } catch (Exception e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
            return false;
        }

        return true;
    }
    //checkout looking
    public boolean checkout(int reservationId) {

    Connection conn = null;

    try {
        conn = DatabaseManager.getInstance().getConnection();
        conn.setAutoCommit(false);

        //  Find room linked to reservation
        int roomId = reservationDAO.findRoomIdByReservation(conn, reservationId);

        if (roomId == -1) {
            throw new Exception("Reservation not found or already checked out");
        }

        //  Update reservation status
        boolean reservationUpdated =
                reservationDAO.updateReservationStatus(conn, reservationId, "CHECKED_OUT");

        if (!reservationUpdated) {
            throw new Exception("Failed to update reservation status");
        }

        //  Update room status back to AVAILABLE
        boolean roomUpdated =
                roomDAO.updateRoomStatus(conn, roomId, "AVAILABLE");

        if (!roomUpdated) {
            throw new Exception("Failed to update room status");
        }

        conn.commit();
        return true;

    } catch (Exception e) {

        try {
            if (conn != null) conn.rollback();
        } catch (Exception ignored) {}

        e.printStackTrace();
        return false;

    } finally {
        try {
            if (conn != null) conn.setAutoCommit(true);
        } catch (Exception ignored) {}
    }
}
    //generate bills
    public String generateBillDetails(int reservationId) {

    try {
        Connection conn = DatabaseManager.getInstance().getConnection();

        Reservation reservation =
                reservationDAO.findById(conn, reservationId);

        if (reservation == null) {
            return "<h2>Reservation Not Found</h2>";
        }

        Bill bill =
                billDAO.findBillByReservationId(conn, reservationId);

        if (bill == null) {
            return "<h2>Bill Not Found</h2>";
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.parse(reservation.getCheckIn()),
                java.time.LocalDate.parse(reservation.getCheckOut())
        );

        return "<h2>Bill Details</h2>" +
                "<p><strong>Reservation ID:</strong> " + reservationId + "</p>" +
                "<p><strong>Room ID:</strong> " + reservation.getRoomId() + "</p>" +
                "<p><strong>Check-In:</strong> " + reservation.getCheckIn() + "</p>" +
                "<p><strong>Check-Out:</strong> " + reservation.getCheckOut() + "</p>" +
                "<p><strong>Nights:</strong> " + nights + "</p>" +
                "<p><strong>Total Amount:</strong> Rs. " + bill.getTotalAmount() + "</p>" +
                "<br><a href='/dashboardPage'>Back</a>";

    } catch (Exception e) {
        e.printStackTrace();
    }

    return "<h2>Error Generating Bill</h2>";
}
    //Generating Bill JSon
   public String generateBillJson(int reservationId) {

    try {
        Connection conn = DatabaseManager.getInstance().getConnection();

        String sql =
            "SELECT r.reservation_id, g.nic, g.name, " +
            "rm.room_number, r.check_in, r.check_out, rt.rate " +
            "FROM reservations r " +
            "JOIN guests g ON r.guest_id = g.guest_id " +
            "JOIN room rm ON r.room_id = rm.room_id " +
            "JOIN room_types rt ON rm.type_id = rt.type_id " +
            "WHERE r.reservation_id = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, reservationId);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {

            String nic = rs.getString("nic");
            String name = rs.getString("name");
            String roomNumber = rs.getString("room_number");
            String checkIn = rs.getString("check_in");
            String checkOut = rs.getString("check_out");
            double rate = rs.getDouble("rate");

            long nights = ChronoUnit.DAYS.between(
                    LocalDate.parse(checkIn),
                    LocalDate.parse(checkOut)
            );

            double total = nights * rate;

            return "{"
                    + "\"success\": true,"
                    + "\"reservationId\": " + reservationId + ","
                    + "\"nic\": \"" + nic + "\","
                    + "\"name\": \"" + name + "\","
                    + "\"roomNumber\": \"" + roomNumber + "\","
                    + "\"checkIn\": \"" + checkIn + "\","
                    + "\"checkOut\": \"" + checkOut + "\","
                    + "\"nights\": " + nights + ","
                    + "\"total\": " + total
                    + "}";

        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return "{ \"success\": false }";
}
   
   //Get Reservation JSon 
    public String getReservationsJson(String from, String to) {

    try {

        Connection conn = DatabaseManager.getInstance().getConnection();

        String sql =
                "SELECT r.reservation_id, g.name, r.room_id, r.check_in, r.check_out, r.status " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.guest_id " +
                "WHERE r.check_in BETWEEN ? AND ? " +
                "ORDER BY r.check_in DESC " +
                "LIMIT 30";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, from);
        stmt.setString(2, to);

        ResultSet rs = stmt.executeQuery();

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        while (rs.next()) {

            if (!first) json.append(",");
            first = false;

            json.append("{")
                .append("\"reservationId\":").append(rs.getInt("reservation_id")).append(",")
                .append("\"guestName\":\"").append(rs.getString("name")).append("\",")
                .append("\"roomId\":").append(rs.getInt("room_id")).append(",")
                .append("\"checkIn\":\"").append(rs.getString("check_in")).append("\",")
                .append("\"checkOut\":\"").append(rs.getString("check_out")).append("\",")
                .append("\"status\":\"").append(rs.getString("status")).append("\"")
                .append("}");
        }

        json.append("]");
        return json.toString();

    } catch (Exception e) {
        e.printStackTrace();
        return "[]";
    }
}

//Active reservations - NIC
public String getActiveReservationsByNic(String nic) {

    StringBuilder json = new StringBuilder();
    json.append("[");

    try {
        Connection conn = DatabaseManager.getInstance().getConnection();

        String sql =
            "SELECT r.reservation_id, r.check_in, r.check_out, rt.rate " +
            "FROM reservations r " +
            "JOIN guests g ON r.guest_id = g.guest_id " +
            "JOIN room rm ON r.room_id = rm.room_id " +
            "JOIN room_types rt ON rm.type_id = rt.type_id " +
            "WHERE g.nic = ? AND r.status = 'ACTIVE'";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nic);

        ResultSet rs = stmt.executeQuery();

        boolean first = true;

        while (rs.next()) {

            if (!first) json.append(",");
            first = false;

            String checkIn = rs.getString("check_in");
            String checkOut = rs.getString("check_out");
            double rate = rs.getDouble("rate");

            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.parse(checkIn),
                java.time.LocalDate.parse(checkOut)
            );

            double total = nights * rate;

            json.append("{")
                .append("\"reservationId\":").append(rs.getInt("reservation_id")).append(",")
                .append("\"checkIn\":\"").append(checkIn).append("\",")
                .append("\"checkOut\":\"").append(checkOut).append("\",")
                .append("\"nights\":").append(nights).append(",")
                .append("\"amount\":").append(total)
                .append("}");
        }

        json.append("]");

    } catch (Exception e) {
        e.printStackTrace();
        return "[]";
    }

    return json.toString();
}
}
