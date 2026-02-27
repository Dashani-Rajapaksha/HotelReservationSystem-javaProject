package com.hotel.model;

public class Guest {

    private int guestId;
    private String name;
    private String address;
    private String contact;
    private String nic;

    public Guest(String name, String address, String contact, String nic) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.nic = nic;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getNic() {
        return nic;
    }
}

