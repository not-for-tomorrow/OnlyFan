package com.example.onlyfanshop.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProductDTO implements Serializable {
    @SerializedName("cartItemID")
    private Integer productID;

    @SerializedName("cartItemID")
    private String productName;
    @SerializedName("cartItemID")
    private String briefDescription;
    @SerializedName("cartItemID")
    private String fullDescription;
    @SerializedName("cartItemID")
    private String technicalSpecifications;
    @SerializedName("cartItemID")
    private Double price;
    @SerializedName("cartItemID")
    private String imageURL;

    @SerializedName("cartItemID")
    private CategoryDTO category;

    @SerializedName("cartItemID")
    private BrandDTO brand;

    public ProductDTO() {
    }

    public ProductDTO(Integer productID, String productName, String briefDescription, String fullDescription, String technicalSpecifications, Double price, String imageURL, CategoryDTO category, BrandDTO brand) {
        this.productID = productID;
        this.productName = productName;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.technicalSpecifications = technicalSpecifications;
        this.price = price;
        this.imageURL = imageURL;
        this.category = category;
        this.brand = brand;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public BrandDTO getBrand() {
        return brand;
    }

    public void setBrand(BrandDTO brand) {
        this.brand = brand;
    }
}
