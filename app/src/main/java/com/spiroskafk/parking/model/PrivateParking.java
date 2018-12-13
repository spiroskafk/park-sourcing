package com.spiroskafk.parking.model;

public class PrivateParking
{
    public String name;
    public String address;
    public String email;
    private String hourlyCharge;
    private double latit;
    private int capacity;
    private int occupied;
    private String entrance;

    public PrivateParking(String name, String address, String email, String hourlyCharge, double latit, double longtit, int capacity, int occupied, String entrance) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.hourlyCharge = hourlyCharge;
        this.latit = latit;
        this.capacity = capacity;
        this.occupied = occupied;
        this.entrance = entrance;
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


    public PrivateParking(String name) {
        this.name = name;
    }


    public String getHourlyCharge() {

        return hourlyCharge;
    }

    public void setHourlyCharge(String hourlyCharge) {
        this.hourlyCharge = hourlyCharge;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
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
