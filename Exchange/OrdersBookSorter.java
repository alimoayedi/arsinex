package com.arsinex.com.Exchange;

import com.arsinex.com.Objects.MarketOrderObject;

import java.util.Comparator;

public class OrdersBookSorter implements Comparator<MarketOrderObject> {

    private boolean ascending = true; // default is ascending order

    public OrdersBookSorter(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(MarketOrderObject orderOne, MarketOrderObject orderTwo) {
        if (ascending) {
            return orderOne.getPrice().compareTo(orderTwo.getPrice());
        } else {
            return orderTwo.getPrice().compareTo(orderOne.getPrice());
        }
    }

}
