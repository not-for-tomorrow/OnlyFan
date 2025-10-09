package com.example.onlyfanshop.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CategoryDTO implements Serializable {

    // Backend trả "id", còn trong code cũ bạn dùng categoryID -> map cả hai
    @SerializedName(value = "categoryID", alternate = {"id"})
    private Integer categoryID;

    // Backend trả "name", còn trong code cũ bạn dùng categoryName -> map cả hai
    @SerializedName(value = "categoryName", alternate = {"name"})
    private String categoryName;

    public Integer getCategoryID() { return categoryID; }
    public void setCategoryID(Integer categoryID) { this.categoryID = categoryID; }

    public String getCategoryName() { return categoryName != null ? categoryName : ""; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // Bridging để adapter gọi getId(), getName()
    public Integer getId() { return categoryID; }
    public void setId(Integer id) { this.categoryID = id; }

    public String getName() { return getCategoryName(); }
    public void setName(String name) { setCategoryName(name); }
}