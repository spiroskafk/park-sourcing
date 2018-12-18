package com.spiroskafk.parking.model;

public class User {
    public String name;
    public String email;
    public String type;
    public String rating;
    private String parkingHouseId;
    public int rewardPoints;
    public int reports;
    private boolean isParked;
    private long lastReportTimestamp;
    private double latit;
    private double longtit;

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

    public User(String name, String email, String type, String rating, String parkingHouseId,
                int rewardPoints, int reports, boolean isParked, long lastReportTimestamp,
                double latit, double longtit) {
        this.name = name;
        this.email = email;
        this.type = type;
        this.rating = rating;
        this.parkingHouseId = parkingHouseId;
        this.rewardPoints = rewardPoints;
        this.reports = reports;
        this.isParked = isParked;
        this.lastReportTimestamp = lastReportTimestamp;
        this.latit = latit;
        this.longtit = longtit;
    }

    public String getParkingHouseId() {
        return parkingHouseId;
    }

    public void setParkingHouseId(String parkingHouseId) {
        this.parkingHouseId = parkingHouseId;
    }

    public long getLastReportTimestamp() {
        return lastReportTimestamp;
    }

    public void setLastReportTimestamp(long lastReportTimestamp) {
        this.lastReportTimestamp = lastReportTimestamp;
    }

    public boolean isParked() {
        return isParked;
    }

    public void setParked(boolean parked) {
        isParked = parked;
    }


    public User() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }
}
