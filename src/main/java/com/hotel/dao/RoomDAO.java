package com.hotel.dao;

import com.hotel.database.DatabaseManager;
import com.hotel.model.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public int findAvailableRoom(Connection conn, int typeId, String checkIn, String checkOut){

        String sql =
        "SELECT r.room_id " +
        "FROM room r " +
        "WHERE r.type_id = ? " +
        "AND r.room_id NOT IN ( " +
        "SELECT room_id FROM reservations " +
        "WHERE (check_in < ? AND check_out > ?) " +
        ") LIMIT 1";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, typeId);
            stmt.setString(2, checkOut);
            stmt.setString(3, checkIn);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("room_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // no room available
    }
    
    public List<Room> findAvailableRoomsByTypeAndDates(
        Connection conn,
        int typeId,
        String checkIn,
        String checkOut) {

    List<Room> rooms = new ArrayList<>();

    String sql =
        "SELECT r.room_id, r.room_number " +
        "FROM room r " +
        "WHERE r.type_id = ? " +
        "AND r.room_id NOT IN ( " +
        "   SELECT room_id FROM reservations " +
        "   WHERE (check_in < ? AND check_out > ?) " +
        ")";

    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, typeId);
        stmt.setString(2, checkOut);
        stmt.setString(3, checkIn);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Room room = new Room();
            room.setRoomId(rs.getInt("room_id"));
            room.setRoomNumber(rs.getString("room_number"));
            rooms.add(room);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return rooms;
}
    public boolean updateRoomStatus(Connection conn, int roomId, String status) {

    String sql = "UPDATE room SET status = ? WHERE room_id = ?";

    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, status);
        stmt.setInt(2, roomId);

        int rows = stmt.executeUpdate();
        return rows > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}
    
}