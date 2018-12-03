package com.spiroskafk.parking.model;

public class InfoWindowData {
    private String address;
    private String spaces;
    private String distance;
    private String title;

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
