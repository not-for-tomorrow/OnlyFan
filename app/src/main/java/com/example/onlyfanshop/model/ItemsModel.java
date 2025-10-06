package com.example.onlyfanshop.model;

import java.io.Serializable;

public class ItemsModel implements Serializable {
    private String title;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecifications;
    private String price;
    private String oldPrice;
    private String offPercent;
    private String imageUrl;
    private String category;
    private String brand;

    // Constructor không tham số
    public ItemsModel() {
    }

    // Constructor đầy đủ tham số
    public ItemsModel(
            String title,
            String briefDescription,
            String fullDescription,
            String technicalSpecifications,
            String price,
            String oldPrice,
            String offPercent,
            String imageUrl,
            String category,
            String brand
    ) {
        this.title = title;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.technicalSpecifications = technicalSpecifications;
        this.price = price;
        this.oldPrice = oldPrice;
        this.offPercent = offPercent;
        this.imageUrl = imageUrl;
        this.category = category;
        this.brand = brand;
    }

    // Getter và Setter
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getBriefDescription() {
        return briefDescription;
    }
    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }
    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications;
    }
    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getOldPrice() {
        return oldPrice;
    }
    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getOffPercent() {
        return offPercent;
    }
    public void setOffPercent(String offPercent) {
        this.offPercent = offPercent;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
}
