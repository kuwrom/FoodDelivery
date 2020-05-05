package com.habeshastudio.fooddelivery.models;

/**
 * Created by kibrom on 2019/11/17.
 */

public class Food {
    private String Name, Image, Description, Price, Discount, MenuId, FoodId, AvailabilityFlag;
    boolean hasFlavour = false;

    public Food() {
    }

    public Food(String name, String image, String description, String price, String discount, String menuId, String foodId, String availabilityFlag, boolean hasF) {
        Name = name;
        Image = image;
        Description = description;
        Price = price;
        Discount = discount;
        MenuId = menuId;
        FoodId = foodId;
        AvailabilityFlag = availabilityFlag;
        hasFlavour = hasF;
    }

    public boolean isHasFlavour() {
        return hasFlavour;
    }

    public void setHasFlavour(boolean hasFlavour) {
        this.hasFlavour = hasFlavour;
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

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }

    public String getFoodId() {
        return FoodId;
    }

    public void setFoodId(String foodId) {
        FoodId = foodId;
    }

    public String getAvailabilityFlag() {
        return AvailabilityFlag;
    }

    public void setAvailabilityFlag(String availabilityFlag) {
        AvailabilityFlag = availabilityFlag;
    }
}
