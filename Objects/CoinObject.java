package com.arsinex.com.Objects;

import java.util.ArrayList;

public class CoinObject {

    private String market_id, asset_id, baseCurrencySymbol, currencyName, currencySymbol, price, priceChange, imgURL;
    private ArrayList<Object> priceHistoryList = new ArrayList<Object>();

    public CoinObject(String market_id, String asset_id, String baseCurrencySymbol, String currencyName, String currencySymbol, String price, String priceChange, ArrayList<Object> priceHistoryList, String imgURL) {
        this.market_id = market_id;
        this.asset_id = asset_id;
        this.baseCurrencySymbol = baseCurrencySymbol;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
        this.price = price;
        this.priceChange = priceChange;
        this.priceHistoryList = priceHistoryList;
        this.imgURL = imgURL;
    }


    public String getMarket_id() {
        return market_id;
    }
    public String getAsset_id() {
        return asset_id;
    }
    public String getBaseCurrencySymbol() {
        return baseCurrencySymbol;
    }
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    public String getCurrencyName() {
        return currencyName;
    }
    public String getPrice() {
        return price;
    }
    public String getPriceChange() {
        return priceChange;
    }
    public ArrayList<Object> getPriceHistoryList() { return priceHistoryList; }
    public String getImgURL() {
        return imgURL;
    }
}
