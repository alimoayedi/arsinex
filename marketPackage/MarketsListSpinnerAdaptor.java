package com.arsinex.com.marketPackage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arsinex.com.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MarketsListSpinnerAdaptor extends ArrayAdapter<String> {

    public MarketsListSpinnerAdaptor(Context context, ArrayList<String> markets_list) {
        super(context, 0, markets_list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable @org.jetbrains.annotations.Nullable View convertView, @NonNull @NotNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView,
                          ViewGroup parent)
    {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.market_list_spinner, parent, false);
        }

        TextView lblMarketName = convertView.findViewById(R.id.lblMarketName);
        String market_item = getItem(position);

        // It is used the name to the TextView when the
        // current item is not null.
        if (!market_item.equals(null)) {
            lblMarketName.setText(market_item);
        }
        return convertView;
    }
}
