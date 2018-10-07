package com.spiroskafk.parking.model;

public class Company
{
    public String name;
    public String address;
    public String email;
    public String type;

    public Company() {}
    public Company(String name, String address, String email, String type) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getType() { return type; }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setType(String type) { this.type = type; }
}
