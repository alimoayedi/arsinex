package com.arsinex.com.Objects;

import android.util.Log;

import com.arsinex.com.Utilities.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MarketOrderHistoryObject {
    private String side, type, price, amount;
    Double timeStamp;

    public MarketOrderHistoryObject(String side, String type, String price, String amount, Double timeStamp) {
        this.side = side;
        this.type = type;
        this.price = price;
        this.amount = amount;
        this.timeStamp = timeStamp;
    }

    public String getSide() {
        return side;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return new Utils().timeStampToHumanTime(timeStamp, "dd.MM.yyyy, HH:mm");
    }
}