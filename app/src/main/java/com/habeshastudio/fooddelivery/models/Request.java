package com.habeshastudio.fooddelivery.models;

import java.util.List;

/**
 * Created by kibrom on 2019/11/20.
 */

public class Request {
    private String phone;
    private String name;
    private String address;
    private String total;
    private String status;
    private String comment;
    private String paymentState;
    private String paymentMethod;
    private String latLng;
    private String restaurantId;
    private String orderHandler;
    private List<Order> foods;
    private boolean partial = false;

    public Request() {
    }


    public Request(String phone, String name, String address, String total, String status, String comment, String paymentState, String paymentMethod, String latLng, List<Order> foods, boolean partial, String restaurantId, String orderHandler) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.comment = comment;
        this.paymentState = paymentState;
        this.paymentMethod = paymentMethod;
        this.latLng = latLng;
        this.foods = foods;
        this.partial = partial;
        this.restaurantId = restaurantId;
        this.orderHandler = orderHandler;
    }

    public String getOrderHandler() {
        return orderHandler;
    }

    public void setOrderHandler(String orderHandler) {
        this.orderHandler = orderHandler;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }
}
