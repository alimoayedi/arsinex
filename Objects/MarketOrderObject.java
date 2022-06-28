package com.arsinex.com.Objects;

public class MarketOrderObject {
    private String price, amount, date, quantity;

    public MarketOrderObject(String price, String amount, String date, String quantity) {
        this.price = price;
        this.amount = amount;
        this.date = date;
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getQuantity() {
        return quantity;
    }

}
