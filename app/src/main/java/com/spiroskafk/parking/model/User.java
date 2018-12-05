package com.spiroskafk.parking.model;

public class User {
    public String name;
    public String email;
    public String type;
    public String rating;
    public int rewardPoints;
    public int reports;

    public User(String name, String email, String type, String rating, int rewardPoints, int reports) {
        this.name = name;
        this.email = email;
        this.type = type;
        this.rating = rating;
        this.rewardPoints = rewardPoints;
        this.reports = reports;
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
