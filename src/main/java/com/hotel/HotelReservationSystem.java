package com.hotel;

import com.hotel.dao.GuestDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
 import com.hotel.dao.BillDAO;
import com.hotel.model.Bill;
import com.hotel.service.BillService;


public class HotelReservationSystem {

    public static void main(String[] args) {

        GuestDAO guestDAO = new GuestDAO();
        RoomDAO roomDAO = new RoomDAO();
        ReservationDAO reservationDAO = new ReservationDAO();

        // 1️⃣ Save or get guest
        Guest guest = new Guest(
                "Surani De Mel",
                "Colombo",
                "07148631005",
                "199012345678"
        );

        int guestId = guestDAO.saveOrGetGuest(guest);

        if (guestId == -1) {
            System.out.println("Guest creation failed.");
            return;
        }

        // 2️⃣ Check room availability
        int roomId = roomDAO.findAvailableRoom(5, "2025-02-10", "2025-02-15");

        if (roomId == -1) {
            System.out.println("No rooms available.");
            return;
        }

        // 3️⃣ Save reservation
        Reservation reservation = new Reservation(
                guestId,
                roomId,
                "2025-02-10",
                "2025-02-15"
        );

        int reservationId = reservationDAO.saveReservation(reservation);

        if (reservationId != -1) {
            System.out.println("Reservation successful. ID: " + reservationId);
        } else {
            System.out.println("Reservation failed.");
        }
 
// After reservation is created
BillService billService = new BillService();
BillDAO billDAO = new BillDAO();

double total = billService.calculateTotal(reservationId);

Bill bill = new Bill(reservationId, total);

int billId = billDAO.saveBill(bill);

if (billId != -1) {
    System.out.println("Bill generated. Total: " + total);
} else {
    System.out.println("Bill generation failed.");
}
    }
}