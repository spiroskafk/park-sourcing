package com.spiroskafk.parking.model;

public class PrivateParking
{
    public String name;
    public String address;
    public String email;
    private float hourlyCharge;
    private double latit;
    private int capacity;
    private int occupied;

    public PrivateParking(String name, String address, String email, float hourlyCharge, int capacity, int occupied, double latit, double longtit) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.hourlyCharge = hourlyCharge;
        this.latit = latit;
        this.capacity = capacity;
        this.occupied = occupied;
        this.longtit = longtit;
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

    public PrivateParking(String name, String address, String email, float hourlyCharge, double latit, double longtit) {
        this.name = name;

        this.address = address;
        this.email = email;
        this.hourlyCharge = hourlyCharge;
        this.latit = latit;
        this.longtit = longtit;
    }

    public float getHourlyCharge() {

        return hourlyCharge;
    }

    public void setHourlyCharge(float hourlyCharge) {
        this.hourlyCharge = hourlyCharge;
    }

    private double longtit;

    public PrivateParking() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
