
package com.hotel.server;
import com.hotel.service.AuthService;
import com.hotel.service.ReservationService;
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
import java.util.*;

import com.hotel.dao.GuestDAO;
import com.hotel.dao.GuestDAO.GuestSaveResult;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Guest;
import com.hotel.database.DatabaseManager;
import com.hotel.model.BookingResult;

public class WebServer {

    private static final HashMap<String, String> sessions = new HashMap<>();

    // ===============================
    // AUTH CHECK
    // ===============================
    private static boolean isAuthenticated(HttpExchange exchange) {

        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) return false;

        for (String header : cookies) {
            String[] cookieArray = header.split(";");
            for (String cookie : cookieArray) {
                cookie = cookie.trim();
                if (cookie.startsWith("SESSIONID=")) {
                    String sessionId = cookie.substring("SESSIONID=".length());
                    return sessions.containsKey(sessionId);
                }
            }
        }
        return false;
    }

    private static void redirectToLogin(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", "/");
        exchange.sendResponseHeaders(302, -1);
    }

    public static void start() throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ===============================
        // LOGIN
        // ===============================
        server.createContext("/login", exchange -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                String username = params.get("username");
                String password = params.get("password");

                AuthService authService = new AuthService();
                boolean success = authService.login(username, password);

                if (success) {

                    String sessionId = UUID.randomUUID().toString();
                    sessions.put(sessionId, username);

                    exchange.getResponseHeaders().add("Set-Cookie",
                            "SESSIONID=" + sessionId + "; Path=/; HttpOnly");

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

        // ===============================
        // LOGOUT
        // ===============================
        server.createContext("/logout", exchange -> {

            List<String> cookies = exchange.getRequestHeaders().get("Cookie");

            if (cookies != null) {
                for (String header : cookies) {
                    String[] cookieArray = header.split(";");
                    for (String cookie : cookieArray) {
                        cookie = cookie.trim();
                        if (cookie.startsWith("SESSIONID=")) {
                            String sessionId = cookie.substring("SESSIONID=".length());
                            sessions.remove(sessionId);
                        }
                    }
                }
            }

            redirectToLogin(exchange);
        });

        // ===============================
        // PROTECTED PAGES
        // ===============================
        server.createContext("/registerPage", exchange -> {
            if (!isAuthenticated(exchange)) { redirectToLogin(exchange); return; }
            byte[] page = Files.readAllBytes(Paths.get("web/register.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/reservationPage", exchange -> {
            if (!isAuthenticated(exchange)) { redirectToLogin(exchange); return; }
            byte[] page = Files.readAllBytes(Paths.get("web/reservation.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/billPage", exchange -> {
            if (!isAuthenticated(exchange)) { redirectToLogin(exchange); return; }
            byte[] page = Files.readAllBytes(Paths.get("web/bill.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });

        server.createContext("/checkoutPage", exchange -> {
            if (!isAuthenticated(exchange)) { redirectToLogin(exchange); return; }
            byte[] page = Files.readAllBytes(Paths.get("web/checkout.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        });
        
        // ===============================
        // REGISTER PROCESS (PROTECTED)
        // ===============================
        server.createContext("/register", exchange -> {

            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String formData = readFormData(exchange);
                HashMap<String, String> params = parseFormData(formData);

                String name = params.get("name");
                String address = params.get("address");
                String contact = params.get("contact");
                String nic = params.get("nic");

                Connection conn = DatabaseManager.getInstance().getConnection();
                GuestDAO guestDAO = new GuestDAO();

                Guest guest = new Guest(name, address, contact, nic);
                GuestSaveResult result = guestDAO.saveOrGetGuest(conn, guest);

                String jsonResponse;

                if (result != null) {

                    if (result.isNew()) {
                        jsonResponse = "{ \"success\": true, \"message\": \"Guest Registered Successfully!\" }";
                    } else {
                        jsonResponse = "{ \"success\": false, \"message\": \"Guest with this NIC already exists!\" }";
                    }

                } else {
                    jsonResponse = "{ \"success\": false, \"message\": \"Registration Failed!\" }";
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            }
        });

    // ===============================
    // HELP PAGE (PROTECTED)
    // ===============================
    server.createContext("/helpPage", exchange -> {

        if (!isAuthenticated(exchange)) { 
            redirectToLogin(exchange); 
            return; 
        }

        byte[] page = Files.readAllBytes(Paths.get("web/help.html"));
        exchange.sendResponseHeaders(200, page.length);
        exchange.getResponseBody().write(page);
        exchange.getResponseBody().close();
    });
    // ===============================
    // dashboard 
    // ===============================
    server.createContext("/dashboard", exchange -> {
    if (!isAuthenticated(exchange)) { 
        redirectToLogin(exchange); 
        return; 
    }

    byte[] page = Files.readAllBytes(Paths.get("web/dashboard.html"));
    exchange.sendResponseHeaders(200, page.length);
    exchange.getResponseBody().write(page);
    exchange.getResponseBody().close();
    });
    
    // ===============================
    // GUEST LOOKUP (PROTECTED API)
    // ===============================
    server.createContext("/guest", exchange -> {

        if (!isAuthenticated(exchange)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String query = exchange.getRequestURI().getQuery();

        if (query == null || !query.startsWith("nic=")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String nic = URLDecoder.decode(query.substring(4), StandardCharsets.UTF_8);

        Connection conn = DatabaseManager.getInstance().getConnection();
        GuestDAO guestDAO = new GuestDAO();
        Guest guest = guestDAO.findByNic(conn, nic);

        String json;

        if (guest != null) {
            json = "{ \"exists\": true, " +
                    "\"name\": \"" + guest.getName() + "\", " +
                    "\"address\": \"" + guest.getAddress() + "\", " +
                    "\"contact\": \"" + guest.getContact() + "\" }";
        } else {
            json = "{ \"exists\": false }";
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    });

    // ===============================
    // AVAILABLE ROOMS (PROTECTED API)
    // ===============================
    server.createContext("/availableRooms", exchange -> {

        if (!isAuthenticated(exchange)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        HashMap<String, String> params = parseFormData(query);

        String typeIdStr = params.get("typeId");
        String checkIn = params.get("checkIn");
        String checkOut = params.get("checkOut");

        if (typeIdStr == null || checkIn == null || checkOut == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        int typeId = Integer.parseInt(typeIdStr);

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
    
    // ===============================
    // BOOK ROOM (PROTECTED)
    // ===============================
    server.createContext("/book", exchange -> {

        if (!isAuthenticated(exchange)) {
            redirectToLogin(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

            String formData = readFormData(exchange);
            HashMap<String, String> params = parseFormData(formData);

            ReservationService service = new ReservationService();

            int roomId = Integer.parseInt(params.get("roomId"));

            BookingResult result = service.bookRoom(
                    params.get("name"),
                    params.get("address"),
                    params.get("contact"),
                    params.get("nic"),
                    roomId,
                    params.get("checkIn"),
                    params.get("checkOut")
            );

            String jsonResponse;

            if (result.isSuccess()) {

                jsonResponse =
                        "{ \"success\": true, " +
                        "\"message\": \"Booking Confirmed! Reservation ID: "
                        + result.getReservationId() +
                        " | Total: Rs. " + result.getTotalAmount() +
                        "\" }";

            } else {
                jsonResponse =
                        "{ \"success\": false, \"message\": \"Booking Failed!\" }";
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    });
    
    // ===============================
    // CHECK-OUT (PROTECTED)
    // ===============================
    server.createContext("/checkout", exchange -> {

    if (!isAuthenticated(exchange)) {
        redirectToLogin(exchange);
        return;
    }

    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

        String formData = readFormData(exchange);
        HashMap<String, String> params = parseFormData(formData);

        int reservationId = Integer.parseInt(params.get("reservationId"));

        ReservationService service = new ReservationService();
        boolean success = service.checkout(reservationId);

        String jsonResponse;

        if (success) {
            jsonResponse = "{ \"success\": true, \"message\": \"Checkout Successful!\" }";
        } else {
            jsonResponse = "{ \"success\": false, \"message\": \"Checkout Failed!\" }";
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
    });
    // ===============================
    // LOAD RESERVATIONS (PROTECTED)
    // ===============================
    server.createContext("/reservations", exchange -> {

        if (!isAuthenticated(exchange)) {
            redirectToLogin(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> params = parseFormData(query);

            String from = params.get("from");
            String to = params.get("to");

            ReservationService service = new ReservationService();
            String json = service.getReservationsJson(from, to);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    });

    // ===============================
    // ACTIVE RESERVATIONS BY NIC
    // ===============================
    server.createContext("/activeReservationsByNic", exchange -> {

        if (!isAuthenticated(exchange)) {
            redirectToLogin(exchange);
            return;
        }

        String query = exchange.getRequestURI().getQuery();

        if (query == null || !query.startsWith("nic=")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String nic = URLDecoder.decode(query.substring(4), StandardCharsets.UTF_8);

        ReservationService service = new ReservationService();
        String json = service.getActiveReservationsByNic(nic);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    });

    // ===============================
    // BILL GENERATING
    // ===============================
        server.createContext("/bill", exchange -> {

        if (!isAuthenticated(exchange)) {
            redirectToLogin(exchange);
            return;
        }

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
        // ===============================
        // AVAILABLE ROOMS PAGE
        // ===============================
        server.createContext("/availableRoomsPage", exchange -> {

            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            byte[] page = Files.readAllBytes(Paths.get("web/availableRooms.html"));
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.getResponseBody().close();
        }); 
        
        // ===============================
// LOAD ALL ROOMS API
// ===============================
server.createContext("/allRooms", exchange -> {

    if (!isAuthenticated(exchange)) {
        exchange.sendResponseHeaders(401, -1);
        return;
    }

    ReservationService service = new ReservationService();
    String json = service.getAllRoomsJson();

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
});

    //---------------------------------------------------------
    //AVAILABLE ROOM BY DATE
    //---------------------------------------------------------
    server.createContext("/availableRoomsByDate", exchange -> {

    if (!isAuthenticated(exchange)) {
        redirectToLogin(exchange);
        return;
    }

    String query = exchange.getRequestURI().getQuery();
    HashMap<String, String> params = parseFormData(query);

    String checkIn = params.get("checkIn");
    String checkOut = params.get("checkOut");

    if (checkIn == null || checkOut == null) {
        exchange.sendResponseHeaders(400, -1);
        return;
    }

    Connection conn = DatabaseManager.getInstance().getConnection();
    RoomDAO roomDAO = new RoomDAO();

    List<Room> rooms =
        roomDAO.findRoomsWithReservationStatus(conn, checkIn, checkOut);

    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < rooms.size(); i++) {

        Room r = rooms.get(i);

        json.append("{")
            .append("\"roomNumber\":\"").append(r.getRoomNumber()).append("\",")
            .append("\"typeName\":\"").append(r.getTypeName()).append("\",")
            .append("\"rate\":").append(r.getRate()).append(",")
            .append("\"status\":\"").append(r.getStatus()).append("\"")
            .append("}");

        if (i < rooms.size() - 1) json.append(",");
    }
    json.append("]");

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
    });

        // ===============================
        // ROOT (ONLY LOGIN PAGE)
        // ===============================
        server.createContext("/", exchange -> {
            byte[] response = Files.readAllBytes(Paths.get("web/login.html"));
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        });

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
