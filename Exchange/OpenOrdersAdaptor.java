package com.arsinex.com.Exchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.arsinex.com.Objects.OpenOrderObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class OpenOrdersAdaptor extends BaseAdapter {

    private ArrayList<OpenOrderObject> openOrdersList;
    private Context context;

    private Utils utils = new Utils();

    public OpenOrdersAdaptor(Context context, ArrayList<OpenOrderObject> openOrdersList) {
        this.openOrdersList = openOrdersList;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        return openOrdersList.size();
    }

    @Override
    public OpenOrderObject getItem(int index) {
        return openOrdersList.get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        OpenOrderObject openOrderObject = openOrdersList.get(position);
        if(view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.layout_open_order_item, null);

//            TextView lblPair = (TextView) view.findViewById(R.id.lblPair);
            TextView lblAmount = (TextView) view.findViewById(R.id.lblAmount);
            TextView lblPrice = (TextView) view.findViewById(R.id.lblPrice);
            TextView lblCommission = (TextView) view.findViewById(R.id.lblCommission);
            TextView lblDate = (TextView) view.findViewById(R.id.lblDate);

//            String market_name = openOrderObject.getMarket();
//            lblPair.setText(market_name.substring(0, market_name.length() / 2) + "\\" + market_name.substring(market_name.length()/2));
            lblAmount.setText(utils.reduceDecimal(openOrderObject.getAmount(), Integer.parseInt(openOrderObject.getStock_prec())));
            lblPrice.setText(utils.reduceDecimal(openOrderObject.getPrice(), Integer.parseInt(openOrderObject.getMoney_prec())));
            if(openOrderObject.getSide().equals("1")) {
                lblCommission.setText(openOrderObject.getMaker_fee());
            } else {
                lblCommission.setText(openOrderObject.getTaker_fee());
            }
            lblDate.setText(openOrderObject.getDate());
        }
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return openOrdersList.size();
    }

}
