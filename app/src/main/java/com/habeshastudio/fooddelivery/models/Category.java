package com.habeshastudio.fooddelivery.models;

/**
 * Created by kibrom on 2019/11/17.
 */

public class Category {
    private String Name;
    private String Image;
    private String Location;
    private String orderHandler;
    private boolean isOpened;

    public Category(String name, String image, String location, String orderHandler) {
        Name = name;
        Image = image;
        Location = location;
        orderHandler = orderHandler;
    }

    public String getOrderHandler() {
        return orderHandler;
    }

    public Category() {
    }

    public void setOrderHandler(String orderHandler) {
        this.orderHandler = orderHandler;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        this.Location = location;
    }
}
