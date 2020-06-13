package com.habeshastudio.fooddelivery.models;

/**
 * Created by kibrom on 2019/11/16.
 */

public class User {
    private String password = "";
    private String Name, Phone, IsStaff, Image, HomeAddress,
            IsVerified, CreatedAt;
    private Object Balance;

    public User() {
    }


    public User(String name, String phone, String homeAddress, String image) {
        Name = name;
        Phone = phone;
        HomeAddress = homeAddress;
        Image = image;
        IsStaff = "false";
    }

    public User(String username, String phone) {
        Name = username;
        Phone = phone;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getPhone() {

        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        password = password;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getHomeAddress() {
        return HomeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        HomeAddress = homeAddress;
    }

    public Object getBalance() {
        return Balance;
    }

    public void setBalance(Object balance) {
        Balance = balance;
    }

    public String getIsVerified() {
        return IsVerified;
    }

    public void setIsVerified(String isVerified) {
        IsVerified = isVerified;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }
}
