package com.spiroskafk.parking.model;

public class RentParking {

    private String address;
    private String id;
    private String userId;
    private String city;
    private String fromDate;
    private String untilDate;
    private String nos;
    private String comments;
    private String type;
    private Float latit;

    public RentParking() {
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(String untilDate) {
        this.untilDate = untilDate;
    }

    public String getNos() {
        return nos;
    }

    public void setNos(String nos) {
        this.nos = nos;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getLatit() {
        return latit;
    }

    public void setLatit(Float latit) {
        this.latit = latit;
    }

    public Float getLongtit() {
        return longtit;
    }

    public void setLongtit(Float longtit) {
        this.longtit = longtit;
    }

    public RentParking(String address, String userId, String id, String fromDate, String untilDate, String nos, String comments, String type, Float latit, Float longtit) {

        this.address = address;
        this.id = id;
        this.userId = userId;
        this.fromDate = fromDate;
        this.untilDate = untilDate;
        this.nos = nos;
        this.comments = comments;
        this.type = type;
        this.latit = latit;
        this.longtit = longtit;
    }

    private Float longtit;


}
