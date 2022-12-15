package com.anilduyguc.barcodescannerproject.data;

public class SellerData {
    private String title;
    private double price;
    private String url;
    private String name;
    private String imageUrl;
    private String description;
    private String author;
    private String isbnNo;
    private String category;



    public SellerData(String title, double price, String url, String name, String imageUrl, String description, String author, String isbnNo, String category) {
        this.title = title;
        this.price = price;
        this.url = url;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.author = author;
        this.isbnNo = isbnNo;
        this.category = category;
    }

//    public SellerData(String title, double price, String url, String name) {
//        this.title = title;
//        this.price = price;
//        this.url = url;
//        this.name = name;
//    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public String getIsbnNo() {
        return isbnNo;
    }

    public void setIsbnNo(String isbnNo) {
        this.isbnNo = isbnNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
