package com.habeshastudio.fooddelivery.Model;

/**
 * Created by kibrom on 2017/11/16.
 */

public class User {
    String Password = "";
    private String Name, Phone, IsStaff, SecureCode, HomeAddress,
            IsVerified, CreatedAt;
    private Object Balance;

    public User() {
    }


    public User(String name, String password) {
        Name = name;
        Password = password;
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
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
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
