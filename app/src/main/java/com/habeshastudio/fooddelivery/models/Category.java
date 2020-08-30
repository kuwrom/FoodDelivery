package com.habeshastudio.fooddelivery.models;

import com.google.firebase.database.PropertyName;

/**
 * Created by kibrom on 2019/11/17.
 */

public class Category {
    private String Name;
    private String Image;
    private String Location;
    private String orderHandler;
    private String deliveryPrice;
    private Object priority;
    private Object category;
    private boolean opened;

    public Category(String name, String image, String location, String orderHandler, String deliveryPrice, Object priority, Object category, boolean opened) {
        Name = name;
        Image = image;
        Location = location;
        this.priority = priority;
        this.category = category;
        this.opened = opened;
        this.deliveryPrice = deliveryPrice;
        orderHandler = orderHandler;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(String deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    @PropertyName("opened")
    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getOrderHandler() {
        return orderHandler;
    }

    public Category() {
    }

    public int getPriority() {
        if (priority.getClass() == String.class)
            return Integer.parseInt((String) priority);
        else
            return ((Long) priority).intValue();
    }

    public void setPriority(Object priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return String.valueOf(category);
    }

    public void setCategory(Object category) {
        this.category = category;
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
