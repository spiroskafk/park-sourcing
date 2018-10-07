package com.spiroskafk.parking.model;

public class User {
    public String name;
    public String email;
    public String type;

    public User() {}

    public User(String name, String email, String type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getType() { return type; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setType(String type) { this.type = type; }
}
