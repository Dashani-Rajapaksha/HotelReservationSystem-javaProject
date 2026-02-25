package com.hotel;

import com.hotel.service.ReservationService;

public class HotelReservationSystem {

    public static void main(String[] args) {

        ReservationService service = new ReservationService();

        service.bookRoom(
                "Nimal Perera",
                "Colombo",
                "0771112222",
                "199812345678",
                1,
                "2025-03-01",
                "2025-03-05"
        );
    }
}