package com.buyo.adminfx.model;

public class User {
    private int id;
    private String name;
    private String email;
    private boolean admin;
    private String phone;
    private String photoUrl;

    public User() {}

    public User(int id, String name, String email, boolean admin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.admin = admin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
