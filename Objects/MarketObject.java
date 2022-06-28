package com.arsinex.com.Objects;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Comparator;

public class MarketObject {
    private String name;
    private String stock;
    private String money;
    private String money_prec;
    private String stock_prec;
    private String price;
    private ArrayList<Entry> chartData;

    /**
     *
     * @param name
     * @param stock
     * @param money
     * @param money_prec
     * @param stock_prec
     */

    public MarketObject(String name, String stock, String money, String money_prec, String stock_prec) {
        this.name = name; // currency name e.g. BTC
        this.stock = stock; // stock name e.g. BTCTRY
        this.money = money; // money name e.g. TRY
        this.money_prec = money_prec;
        this.stock_prec = stock_prec;
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

    public String getMarketSymbol() {
        return stock + "\\" + money;
    }

    public String getStock_prec() {
        return stock_prec;
    }

    public ArrayList<Entry> getChartData() {
        return chartData;
    }

    public void setChartData(ArrayList<Entry> chartData) {
        this.chartData = chartData;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
