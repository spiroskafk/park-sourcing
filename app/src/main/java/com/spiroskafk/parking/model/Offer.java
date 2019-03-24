package com.spiroskafk.parking.model;

public class Offer {
    public String parkingHouseId;
    public String offer;
    public String fromTime;
    public String untilTime;

    public String getParkingHouseId() {
        return parkingHouseId;
    }

    public String getFromTime() {
        return fromTime;
    }

    public Offer(String parkingHouseId, String offer, String fromTime, String untilTime) {
        this.parkingHouseId = parkingHouseId;
        this.offer = offer;
        this.fromTime = fromTime;
        this.untilTime = untilTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getUntilTime() {
        return untilTime;
    }

    public void setUntilTime(String untilTime) {
        this.untilTime = untilTime;
    }

    public Offer(String parkingHouseId, String offer) {
        this.parkingHouseId = parkingHouseId;
        this.offer = offer;

    }

    public void setParkingHouseId(String parkingHouseId) {
        this.parkingHouseId = parkingHouseId;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public Offer() {}
}
