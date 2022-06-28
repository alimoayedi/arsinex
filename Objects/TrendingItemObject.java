package com.arsinex.com.Objects;

public class TrendingItemObject {

    private String name;
    private String stock;
    private String money;
    private String money_prec;
    private String price;

    public TrendingItemObject(String name, String stock, String money, String money_prec) {
        this.name = name;
        this.stock = stock;
        this.money = money;
        this.money_prec = money_prec;
    }

    public String getName() {
        return name;
    }

    public String getStock() {
        return stock;
    }

    public String getMoney() {
        return money;
    }

    public String getMoney_prec() {
        return money_prec;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
