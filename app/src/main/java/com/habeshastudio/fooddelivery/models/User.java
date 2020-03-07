package com.habeshastudio.fooddelivery.models;

/**
 * Created by kibrom on 2017/11/16.
 */

public class User {
    String password = "";
    private String Name, Phone, IsStaff, SecureCode, HomeAddress,
            IsVerified, CreatedAt;
    private Object Balance;

    public User() {
    }


    public User(String name, String phone) {
        Name = name;
        Phone = phone;
        IsStaff = "false";
    }

    public String getSecureCode() {
        return SecureCode;
    }

    public void setSecureCode(String secureCode) {
        SecureCode = secureCode;
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
