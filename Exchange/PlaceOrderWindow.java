package com.arsinex.com.Exchange;

import android.content.Context;
import android.view.View;

import org.json.JSONObject;

public class PlaceOrderWindow {

    private Context context;
    private View view;
    private int action;
    private JSONObject data;

    public PlaceOrderWindow(Context context, View view, int action, JSONObject data) {
        this.context = context;
        this.view = view;
        this.action = action;
        this.data = data;
    }
}
