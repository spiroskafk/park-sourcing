package com.spiroskafk.parking.model;

public class StreetParking {

    private double latit;
    private double longtit;
    private String address;
    private String id;
    private String type;

    public StreetParking(double latit, double longtit, String address, String id, String type, int capacity, int occupied, int points) {
        this.latit = latit;
        this.longtit = longtit;
        this.address = address;
        this.id = id;
        this.type = type;
        this.capacity = capacity;
        this.occupied = occupied;
        this.points = points;
    }

    public double getLatit() {
        return latit;
    }

    public void setLatit(double latit) {
        this.latit = latit;
    }

    public double getLongtit() {
        return longtit;
    }

    public void setLongtit(double longtit) {
        this.longtit = longtit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    private int capacity;
    private int occupied;
    private int points;


    public StreetParking() {
    }
}
