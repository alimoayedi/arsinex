package com.arsinex.com.Exchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.arsinex.com.Objects.MarketOrderObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketAction;

public class MarketOrdersAdaptor extends RecyclerView.Adapter<MarketOrdersAdaptor.MyViewHolder> {

    private ArrayList<MarketOrderObject> marketOrdersList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private MarketAction action;

    private Utils utils = new Utils();

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView lblPrice, lblQuantity;

        private OnItemClickListener onItemClickListener;

        MyViewHolder(@NotNull View view, OnItemClickListener onItemClickListener) {
            super(view);
            lblPrice = view.findViewById(R.id.lblPrice);
            lblQuantity = view.findViewById(R.id.lblQuantity);
            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onOrderBookItemClick(action, marketOrdersList.get(getLayoutPosition()));
        }

    }

    public interface OnItemClickListener {
        void onOrderBookItemClick(MarketAction action, MarketOrderObject orderObject);
    }

    public MarketOrdersAdaptor(Context context, ArrayList<MarketOrderObject> marketOrdersList, MarketAction action, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.marketOrdersList = marketOrdersList;
        this.action = action;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return marketOrdersList.size();
    }

    @NonNull
    @NotNull
    @Override
    public MarketOrdersAdaptor.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_orders_item, parent, false);
        return new MyViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MarketOrdersAdaptor.MyViewHolder holder, int position) {
        MarketOrderObject marketOrderObject = marketOrdersList.get(position);
        holder.lblPrice.setText(utils.reduceDecimal(marketOrderObject.getPrice(),8));
        holder.lblQuantity.setText(utils.reduceDecimal(marketOrderObject.getAmount(), 8));
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

}
