package com.arsinex.com.Exchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.arsinex.com.Objects.MarketOrderHistoryObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class MarketHistoryAdaptor extends RecyclerView.Adapter<MarketHistoryAdaptor.MyViewHolder> {

    private ArrayList<MarketOrderHistoryObject> marketHistoryList;
    private Context context;

    private Utils utils = new Utils();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView lblQty, lblPrice, lblDate, lblSide;
        MyViewHolder(View view) {
            super(view);
            lblQty = view.findViewById(R.id.lblQty);
            lblPrice = view.findViewById(R.id.lblPrice);
            lblDate = view.findViewById(R.id.lblDate);
            lblSide = view.findViewById(R.id.lblSide);
        }
    }

    public MarketHistoryAdaptor(Context context, ArrayList<MarketOrderHistoryObject> marketHistoryList) {
        this.marketHistoryList = marketHistoryList;
        this.context = context;
    }

    @Override
    public int getItemCount() { return marketHistoryList.size(); }

    @NonNull
    @NotNull
    @Override
    public MarketHistoryAdaptor.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MarketHistoryAdaptor.MyViewHolder holder, int position) {
        MarketOrderHistoryObject marketOrderHistoryObject = marketHistoryList.get(position);
        holder.lblQty.setText(utils.reduceDecimal(marketOrderHistoryObject.getAmount(), 8));
        holder.lblPrice.setText(utils.reduceDecimal(marketOrderHistoryObject.getPrice(), 2));
        holder.lblDate.setText(marketOrderHistoryObject.getDate());
        if (marketOrderHistoryObject.getSide().equals("1")) {
            holder.lblSide.setText(context.getResources().getString(R.string.buy));
            holder.lblSide.setTextColor(ContextCompat.getColor(context, R.color.green_1));
        } else if (marketOrderHistoryObject.getSide().equals("2")) {
            holder.lblSide.setText(context.getResources().getString(R.string.sell));
            holder.lblSide.setTextColor(ContextCompat.getColor(context, R.color.red_1));
        }
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

}
