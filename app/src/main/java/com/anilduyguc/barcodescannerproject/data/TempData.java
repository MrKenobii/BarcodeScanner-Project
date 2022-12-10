package com.anilduyguc.barcodescannerproject.data;

public class TempData {
    private String sellerName;
    private double price;
    private String imageUrl;

    public TempData(String sellerName, double price, String imageUrl) {
        this.sellerName = sellerName;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "TempData{" +
                "sellerName='" + sellerName + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
