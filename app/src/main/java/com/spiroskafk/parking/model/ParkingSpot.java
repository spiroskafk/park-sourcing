package com.spiroskafk.parking.model;

public class ParkingSpot {
    private String id;
    private String category;
    private float latitude;
    private float longitude;

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {

        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String description) {
        this.category = description;
    }

    public String getId() {

        return id;
    }

    public String getCategory() {
        return category;
    }

    public ParkingSpot(String id, String category, float latitude, float longitude) {
        this.id = id;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public ParkingSpot() {}
}
