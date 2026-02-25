package com.hotel;

import com.hotel.service.AuthService;

public class HotelReservationSystem {

    public static void main(String[] args) {

        AuthService authService = new AuthService();

        boolean loggedIn = authService.login("admin", "123456");

        if (!loggedIn) {
            return;
        }

        System.out.println("Proceed to booking...");
    }
}