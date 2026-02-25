package com.hotel.service;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.database.DatabaseManager;

import java.sql.Connection;

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
            int typeId,
            String checkIn,
            String checkOut
    ) {

        if (!validateInput(name, contact, nic, checkIn, checkOut)) {
            return new BookingResult(false, 0, 0, 0);
        }

        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // 🔥 Start Transaction

            // 1️⃣ Save or get guest
            Guest guest = new Guest(name, address, contact, nic);
            int guestId = guestDAO.saveOrGetGuest(conn, guest);

            if (guestId == -1) {
                throw new Exception("Guest processing failed");
            }

            // 2️⃣ Find available room
            int roomId = roomDAO.findAvailableRoom(conn, typeId, checkIn, checkOut);

            if (roomId == -1) {
                throw new Exception("No rooms available");
            }

            // 3️⃣ Save reservation
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

            // 4️⃣ Calculate total bill
            double total = billService.calculateTotal(conn, reservationId);

            Bill bill = new Bill(reservationId, total);
            int billId = billDAO.saveBill(conn, bill);

            if (billId == -1) {
                throw new Exception("Bill generation failed");
            }

            conn.commit(); // ✅ Success

            System.out.println("Booking successful!");
            System.out.println("Reservation ID: " + reservationId);
            System.out.println("Room ID: " + roomId);
            System.out.println("Total: " + total);

            return new BookingResult(true, reservationId, roomId, total);

        } catch (Exception e) {

            try {
                if (conn != null) {
                    conn.rollback(); // ❌ Rollback
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

    // 🔹 Validation Method
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

    public boolean checkout(int reservationId) {

    Connection conn = null;

    try {
        conn = DatabaseManager.getInstance().getConnection();
        conn.setAutoCommit(false);

        // 1️⃣ Get room ID from reservation
        int roomId = reservationDAO.getRoomIdByReservationId(conn, reservationId);

        if (roomId == -1) {
            throw new Exception("Reservation not found");
        }

        // 2️⃣ Update reservation status
        boolean reservationUpdated =
                reservationDAO.updateReservationStatus(conn, reservationId, "CHECKED_OUT");

        if (!reservationUpdated) {
            throw new Exception("Failed to update reservation status");
        }

        // 3️⃣ Update room status back to AVAILABLE
        boolean roomUpdated =
                roomDAO.updateRoomStatus(conn, roomId, "AVAILABLE");

        if (!roomUpdated) {
            throw new Exception("Failed to update room status");
        }

        conn.commit();
        return true;

    } catch (Exception e) {

        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (Exception ignored) {}

        e.printStackTrace();
        return false;

    } finally {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        } catch (Exception ignored) {}
    }
}
    //bill generating
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
        public String generateBillJson(int reservationId) {

            try {
                Connection conn = DatabaseManager.getInstance().getConnection();

                Reservation reservation =
                        reservationDAO.findById(conn, reservationId);

                if (reservation == null) {
                    return "{ \"success\": false }";
                }

                Bill bill =
                        billDAO.findBillByReservationId(conn, reservationId);

                if (bill == null) {
                    return "{ \"success\": false }";
                }

                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.parse(reservation.getCheckIn()),
                        java.time.LocalDate.parse(reservation.getCheckOut())
                );

                return "{ " +
                        "\"success\": true, " +
                        "\"reservationId\": " + reservationId + "," +
                        "\"roomId\": " + reservation.getRoomId() + "," +
                        "\"checkIn\": \"" + reservation.getCheckIn() + "\"," +
                        "\"checkOut\": \"" + reservation.getCheckOut() + "\"," +
                        "\"nights\": " + nights + "," +
                        "\"total\": " + bill.getTotalAmount() +
                        "}";

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "{ \"success\": false }";
    }
}