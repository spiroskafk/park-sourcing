package com.spiroskafk.parking.model;

import java.util.Date;

public class ParkingSpot {
    private String userID;
    private double latit;
    private double longtit;
    private int reward;
    private long timestamp;
    private Date date;
    private String parkingHouseID;


    public ParkingSpot(double latit, double longtit, int reward, long timestamp, String parkingHouseID, String userID) {
        this.userID = userID;
        this.latit = latit;
        this.longtit = longtit;
        this.reward = reward;
        this.timestamp = timestamp;
        this.parkingHouseID = parkingHouseID;
    }

    public String getUserID() {

        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getLatit() {
        return latit;
    }

    public void setLatit(float latit) {
        this.latit = latit;
    }

    public double getLongtit() {
        return longtit;
    }

    public void setLongtit(float longtit) {
        this.longtit = longtit;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getParkingHouseID() {
        return parkingHouseID;
    }

    public void setParkingHouseID(String parkingHouseID) {
        this.parkingHouseID = parkingHouseID;
    }

    public ParkingSpot() {}


}
