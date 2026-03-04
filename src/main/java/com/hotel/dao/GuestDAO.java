package com.hotel.dao;

import com.hotel.model.Guest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class GuestDAO {

    public class GuestSaveResult {
        private int guestId;
        private boolean isNew;

        public GuestSaveResult(int guestId, boolean isNew) {
            this.guestId = guestId;
            this.isNew = isNew;
        }

        public int getGuestId() { return guestId; }
        public boolean isNew() { return isNew; }
    }

    // =========================
    // VALIDATION METHODS
    // =========================
    private boolean isValidContact(String contact) {
        return contact != null && contact.matches("\\d{10}");
    }

    private boolean isValidNIC(String nic) {
        return nic != null && nic.matches(".{10,12}");
    }

    // =========================
    // SAVE OR GET GUEST
    // =========================
    public GuestSaveResult saveOrGetGuest(Connection conn, Guest guest) {

        try {

            // ===== VALIDATION =====
            if (!isValidContact(guest.getContact())) {
                throw new RuntimeException("Invalid contact number. Must contain exactly 10 digits.");
            }

            if (!isValidNIC(guest.getNic())) {
                throw new RuntimeException("Invalid NIC. Must be between 10 and 12 characters.");
            }

            // ===============================
            // CHECK EXISTING GUEST
            // ===============================
            String checkSql = "SELECT guest_id FROM guests WHERE NIC = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, guest.getNic());

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return new GuestSaveResult(rs.getInt("guest_id"), false);
            }

            // ===============================
            // INSERT NEW GUEST
            // ===============================
            String insertSql = "INSERT INTO guests (name, address, contact, NIC) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt =
                    conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

            insertStmt.setString(1, guest.getName());
            insertStmt.setString(2, guest.getAddress());
            insertStmt.setString(3, guest.getContact());
            insertStmt.setString(4, guest.getNic());

            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                return new GuestSaveResult(keys.getInt(1), true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // FIND BY NIC
    // =========================
    public Guest findByNic(Connection conn, String nic) {

        String sql = "SELECT * FROM guests WHERE NIC = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nic);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Guest(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contact"),
                        rs.getString("NIC")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}