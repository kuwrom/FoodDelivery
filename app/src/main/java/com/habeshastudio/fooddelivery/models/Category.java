package com.habeshastudio.fooddelivery.models;

import java.util.HashMap;

/**
 * Created by kibrom on 2017/11/17.
 */

public class Category {
    private String Name;
    private String Image;
    private String Location;

    public Category() {
    }

    public Category(String name, String image, String location) {
        Name = name;
        Image = image;
        Location = location;
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
