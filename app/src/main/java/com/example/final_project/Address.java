package com.example.final_project;

public class Address {
    private int id;
    private int user_id;
    private int province_code;
    private String province_name;
    private int district_code;
    private String district_name;
    private int ward_code;
    private String ward_name;
    private String street_address;
    private boolean isDefault;

    // Constructor
    public Address(int id, int user_id, int province_code, String province_name, int district_code,
                   String district_name, int ward_code, String ward_name, String street_address, boolean isDefault) {
        this.id = id;
        this.user_id = user_id;
        this.province_code = province_code;
        this.province_name = province_name;
        this.district_code = district_code;
        this.district_name = district_name;
        this.ward_code = ward_code;
        this.ward_name = ward_name;
        this.street_address = street_address;
        this.isDefault = isDefault;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return user_id; }
    public int getProvinceCode() { return province_code; }
    public String getProvinceName() { return province_name; }
    public int getDistrictCode() { return district_code; }
    public String getDistrictName() { return district_name; }
    public int getWardCode() { return ward_code; }
    public String getWardName() { return ward_name; }
    public String getStreetAddress() { return street_address; }
    public boolean isDefault() { return isDefault; }

    @Override
    public String toString() {
        return street_address + ", " + ward_name + ", " + district_name + ", " + province_name;
    }
}