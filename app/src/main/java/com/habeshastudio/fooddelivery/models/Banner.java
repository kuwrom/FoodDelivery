package com.habeshastudio.fooddelivery.models;

public class Banner {
    private String id, name, image, message;

    public Banner() {
    }

    public Banner(String id, String name, String image, String message) {
        this.id = id;
        this.message = message;
        this.name = name;
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
