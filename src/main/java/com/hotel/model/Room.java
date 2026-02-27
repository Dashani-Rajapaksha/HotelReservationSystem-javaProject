package com.hotel.model;

public class Room {

    private int roomId;
    private String roomNumber;
    private String status;
    private String typeName;
    private double rate;

    public Room() {
    }

    public Room(int roomId, String roomNumber) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getTypeName() {
    return typeName;
}

public void setTypeName(String typeName) {
    this.typeName = typeName;
}

public double getRate() {
    return rate;
}

public void setRate(double rate) {
    this.rate = rate;
}

} 
