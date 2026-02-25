package com.hotel.server;

import com.hotel.service.AuthService;
import com.hotel.service.ReservationService;
import com.hotel.model.BookingResult;
import com.hotel.model.Room;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.hotel.dao.GuestDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Guest;
import com.hotel.database.DatabaseManager;

public class WebServer {

    public static void start() throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // =====================================================
        // Guest Lookup
        // =====================================================
        server.createContext("/guest", exchange -> {
            String query = exchange.getRequestURI().getQuery();

            if (query == null || !query.startsWith("nic=")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String nic = URLDecoder.decode(query.substring(4), StandardCharsets.UTF_8);

            GuestDAO guestDAO = new GuestDAO();
            Connection conn = DatabaseManager.getInstance().getConnection();
            Guest guest = guestDAO.findByNic(conn, nic);

            String response;

            if (guest != null) {
                response = "{ \"exists\": true, " +
                        "\"name\": \"" + guest.getName() + "\", " +
                        "\"address\": \"" + guest.getAddress() + "\", " +
                        "\"contact\": \"" + guest.getContact() + "\" }";
            } else {
                response = "{ \"exists\": false }";
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // =====================================================
        // Dashboard Page Routes (STEP 3)
        // =====================================================

        server.createContext("/registerPage", exchange -> {
            byte[] page = Files.readAllBytes(Paths.get("web/register.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/reservationPage", exchange -> {
            byte[] page = Files.readAllBytes(Paths.get("web/reservation.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/billPage", exchange -> {
            byte[] page = Files.readAllBytes(Paths.get("web/bill.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/helpPage", exchange -> {
            byte[] page = Files.readAllBytes(Paths.get("web/help.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        // =====================================================
        // Available Rooms
        // =====================================================
        server.createContext("/availableRooms", exchange -> {

            String query = exchange.getRequestURI().getQuery();

            if (query == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            HashMap<String, String> params = parseFormData(query);

            int typeId = Integer.parseInt(params.get("typeId"));
            String checkIn = params.get("checkIn");
            String checkOut = params.get("checkOut");

            Connection conn = DatabaseManager.getInstance().getConnection();
            RoomDAO roomDAO = new RoomDAO();

            List<Room> rooms =
                    roomDAO.findAvailableRoomsByTypeAndDates(conn, typeId, checkIn, checkOut);

            StringBuilder json = new StringBuilder();
            json.append("[");

            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);

                json.append("{")
                        .append("\"roomId\":").append(r.getRoomId()).append(",")
                        .append("\"roomNumber\":\"").append(r.getRoomNumber()).append("\"")
                        .append("}");

                if (i < rooms.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // =====================================================
        // Login
        // =====================================================
        server.createContext("/login", exchange -> {

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                String username = params.get("username");
                String password = params.get("password");

                AuthService authService = new AuthService();
                boolean success = authService.login(username, password);

                if (success) {
                    byte[] page = Files.readAllBytes(Paths.get("web/dashboard.html"));
                    exchange.sendResponseHeaders(200, page.length);
                    exchange.getResponseBody().write(page);
                    exchange.getResponseBody().close();
                } else {
                    String response = "<h2>Login Failed!</h2><a href='/'>Try Again</a>";
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                    exchange.getResponseBody().close();
                }
            }
        });
        // =====================================================
        // Booking
        // =====================================================
        server.createContext("/book", exchange -> {

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                ReservationService service = new ReservationService();

                BookingResult result = service.bookRoom(
                        params.get("name"),
                        params.get("address"),
                        params.get("contact"),
                        params.get("nic"),
                        Integer.parseInt(params.get("roomId")),
                        params.get("checkIn"),
                        params.get("checkOut")
                );

                String response;

                if (result.isSuccess()) {

                    response =
                            "<h2>Booking Confirmed!</h2>" +
                            "<p><strong>Reservation ID:</strong> " + result.getReservationId() + "</p>" +
                            "<p><strong>Room ID:</strong> " + result.getRoomId() + "</p>" +
                            "<p><strong>Total Amount:</strong> Rs. " + result.getTotalAmount() + "</p>" +
                            "<br><a href='/checkoutPage'>Go to Checkout</a>" +
                            "<br><br><a href='/reservationPage'>Back</a>";

                } else {
                    response = "<h2>Booking Failed!</h2><a href='/reservationPage'>Try Again</a>";
                }

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();

            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });
        // =====================================================
        // Checkout Page
        // =====================================================
        server.createContext("/checkoutPage", exchange -> {

            byte[] page = Files.readAllBytes(Paths.get("web/checkout.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        // =====================================================
        // Checkout Process
        // =====================================================
        server.createContext("/checkout", exchange -> {

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                int reservationId = Integer.parseInt(params.get("reservationId"));

                ReservationService service = new ReservationService();
                boolean success = service.checkout(reservationId);

                String response;

                if (success) {
                    response = "<h2>Checkout Successful!</h2><a href='/'>Back to Login</a>";
                } else {
                    response = "<h2>Checkout Failed!</h2><a href='/checkoutPage'>Try Again</a>";
                }

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();

            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });
        
        server.createContext("/bill", exchange -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                int reservationId = Integer.parseInt(params.get("reservationId"));

                ReservationService service = new ReservationService();
                String billJson = service.generateBillJson(reservationId);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] bytes = billJson.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            }
        });
        
        /*Guest registration
        server.createContext("/registerPage", exchange -> {
            byte[] page = Files.readAllBytes(Paths.get("web/register.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });*/
        
        // =====================================================
        // Guest Registration Process
        // =====================================================
        server.createContext("/register", exchange -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                String name = params.get("name");
                String address = params.get("address");
                String contact = params.get("contact");
                String nic = params.get("nic");

                System.out.println("Registering Guest: " + name);

                Connection conn = DatabaseManager.getInstance().getConnection();
                GuestDAO guestDAO = new GuestDAO();

                Guest guest = new Guest(name, address, contact, nic);
                int guestId = guestDAO.saveOrGetGuest(conn, guest);

                String response;

                if (guestId != -1) {
                    response = "<h2>Guest Registered Successfully!</h2>" +
                               "<a href='/registerPage'>Register Another</a><br><br>" +
                               "<a href='/'>Back to Login</a>";
                } else {
                    response = "<h2>Registration Failed!</h2>" +
                               "<a href='/registerPage'>Try Again</a>";
                }

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            }
        });

        // =====================================================
        // Root
        // =====================================================
        server.createContext("/", exchange -> {
            byte[] response = Files.readAllBytes(Paths.get("web/login.html"));
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:8080");
    }

    private static String readFormData(HttpExchange exchange) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        return br.readLine();
    }

    private static HashMap<String, String> parseFormData(String formData) {

        HashMap<String, String> map = new HashMap<>();

        if (formData == null) return map;

        String[] pairs = formData.split("&");

        for (String pair : pairs) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {

                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

                map.put(key, value.trim());
            }
        }

        return map;
    }
}