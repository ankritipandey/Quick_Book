package com.example.skd.book_order.Model;

/**
 * Created by 123456 on 2017/11/17.
 */

public class book {
    private String Name, Image, Description, Price, Discount, MenuId, bookId, AvailabilityFlag;

    public book() {
    }

    public book(String name, String image, String description, String price, String discount, String menuId, String bookId, String availabilityFlag) {
        Name = name;
        Image = image;
        Description = description;
        Price = price;
        Discount = discount;
        MenuId = menuId;
        bookId = bookId;
        AvailabilityFlag = availabilityFlag;
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

    public String getbookId() {
        return bookId;
    }

    public void setbookId(String bookId) {
        bookId = bookId;
    }

    public String getAvailabilityFlag() {
        return AvailabilityFlag;
    }

    public void setAvailabilityFlag(String availabilityFlag) {
        AvailabilityFlag = availabilityFlag;
    }
}
