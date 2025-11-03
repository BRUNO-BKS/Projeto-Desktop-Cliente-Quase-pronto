package com.buyo.adminfx.model;

public class Address {
    private final int id;
    private final int userId;
    private final String street;
    private final String number;
    private final String district;
    private final String city;
    private final String state;
    private final String zip;
    private final String complement;
    private final String type;

    public Address(int id, int userId, String street, String number, String district, String city, String state, String zip, String complement, String type) {
        this.id = id; this.userId = userId; this.street = street; this.number = number; this.district = district; this.city = city; this.state = state; this.zip = zip; this.complement = complement; this.type = type;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }
    public String getComplement() { return complement; }
    public String getType() { return type; }
}
