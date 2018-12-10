package com.spiroskafk.parking.model;

public class InfoWindowData {
    private String address;
    private String spaces;
    private String distance;
    private String title;
    private int capacity;
    private int occupied;
    private float hourlyCharge;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    public float getHourlyCharge() {
        return hourlyCharge;
    }

    public void setHourlyCharge(float hourlyCharge) {
        this.hourlyCharge = hourlyCharge;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpaces() {
        return spaces;
    }

    public void setSpaces(String spaces) {
        this.spaces = spaces;
    }

    public InfoWindowData(String address, String spaces, String distance) {
        this.address = address;
        this.spaces = spaces;
        this.distance = distance;
    }

    public InfoWindowData() {}

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
