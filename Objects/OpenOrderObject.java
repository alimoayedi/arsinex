package com.arsinex.com.Objects;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OpenOrderObject {
    private String side, id, market, type, price, maker_fee, taker_fee, amount, money_prec, stock_prec;
    private Double timeStamp;

    /**
     *
     * @param side      determines buy = 1 or sell = 2
     * @param id        user id
     * @param market    market name e.g. "BTCTRY"
     * @param amount    amount of exchanged money
     * @param price     price of exchanged money
     * @param maker_fee buy commission fee
     * @param taker_fee sell commission fee
     * @param type      type defines coin or money
     * @param money_prec    money precision
     * @param stock_prec    coin precision
     * @param timeStamp unix timestamp
     */

    public OpenOrderObject(String side, String id, String market, String amount, String price, String maker_fee, String taker_fee, String type, String money_prec, String stock_prec, Double timeStamp) {
        this.side = side;
        this.id = id;
        this.market = market;
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.maker_fee = maker_fee;
        this.taker_fee = taker_fee;
        this.money_prec = money_prec;
        this.stock_prec = stock_prec;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public String getSide() {
        return side;
    }

    public String getMarket() {
        return market;
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

    public String getMaker_fee() {
        return maker_fee;
    }

    public String getTaker_fee() {
        return taker_fee;
    }

    public String getMoney_prec() {
        return money_prec;
    }

    public String getStock_prec() {
        return stock_prec;
    }

    public String getDate() {
        NumberFormat formatter = new DecimalFormat("#000000000");
        long date_in_millisecond = Long.parseLong(formatter.format(timeStamp))*1000;
        Date date = new java.util.Date(date_in_millisecond);
        return new SimpleDateFormat("dd.MM.yy").format(date);
    }
}