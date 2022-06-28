package com.arsinex.com.marketPackage;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.arsinex.com.FetchAndLoadImage;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class TrendMarketsAdaptor extends RecyclerView.Adapter<TrendMarketsAdaptor.MyViewHolder> {

    private Activity activity;
    private ArrayList<MarketObject> trendingItems;
    private OnItemClickListener onItemClickListener;

    private Utils utils = new Utils();

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView lblCurrencyName, lblCurrencySymbol, lblPrice, lblPriceChange, lblPriceUnit;
        private ImageView imgLogo;
        private LineChart chartLine;

        private OnItemClickListener onItemClickListener;

        MyViewHolder(View view, OnItemClickListener onItemClickListener) {
            super(view);
            lblCurrencyName = view.findViewById(R.id.lblCurrencyName);
            lblCurrencySymbol = view.findViewById(R.id.lblCurrencySymbol);
            lblPrice = view.findViewById(R.id.lblPrice);
            lblPriceChange = view.findViewById(R.id.lbl_change_24h_rate);
            lblPriceUnit = view.findViewById(R.id.lblPriceUnit);
            imgLogo = view.findViewById(R.id.imgLogo);
            chartLine = (LineChart) view.findViewById(R.id.chartLine);
            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onTrendItemClick(trendingItems.get(getLayoutPosition()));
        }
    }

    public interface OnItemClickListener {
        void onTrendItemClick(MarketObject item);
    }

    public TrendMarketsAdaptor(Activity activity, ArrayList<MarketObject> trendingItems, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.trendingItems = trendingItems;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trend_currencies, parent, false);
        return new MyViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        MarketObject marketObject = trendingItems.get(position);

        holder.lblCurrencyName.setText(marketObject.getStock());
        holder.lblCurrencySymbol.setText(marketObject.getStock());
        holder.lblPrice.setText(utils.reduceDecimal(marketObject.getPrice(), Integer.parseInt(marketObject.getMoney_prec())));
        holder.lblPriceUnit.setText(marketObject.getMoney());
        holder.lblPriceChange.setText("");

//        double priceChange = Double.parseDouble(money.getPriceChange());
//        if (priceChange < 0) {
//            holder.lblPriceChange.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.error_red));
//        } else {
//            holder.lblPriceChange.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.green_1));
//        }

        setChartViewProperties(holder.chartLine); // set chart view properties

        LineDataSet lineDataSet = new LineDataSet(marketObject.getChartData(), "lineChart");
        setGraphProperties(lineDataSet); // set properties of graph

        LineData lineData = new LineData(lineDataSet);

        holder.chartLine.setData(lineData);
        holder.chartLine.invalidate();

        if(!marketObject.getStock().equals("null")) {
            FetchAndLoadImage fetchAndLoadImage = new FetchAndLoadImage(activity.getApplicationContext());
            fetchAndLoadImage.setImage(marketObject.getStock(), holder.imgLogo, "ic_" + marketObject.getStock());
        }
    }

    @Override
    public int getItemCount() {
        return trendingItems.size();
    }

    private void setChartViewProperties(LineChart chart) {
        chart.getDescription().setEnabled(false);  // Hide the description
        chart.getLegend().setEnabled(false);   // Hide the legend
        chart.setMinOffset(0f);
        chart.setDrawBorders(false);
        chart.setBackgroundColor(ContextCompat.getColor(activity.getBaseContext(), R.color.transparent));
        chart.setTouchEnabled(false);

        // set left Y axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawZeroLine(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setSpaceTop(0);
        chart.getAxisLeft().setDrawTopYLabelEntry(false);

        // set right Y axis
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawZeroLine(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setSpaceTop(0);
        chart.getAxisRight().setDrawTopYLabelEntry(false);

        // set X axis
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
    }

    private void setGraphProperties(LineDataSet lineDataSet) {
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColor(ContextCompat.getColor(activity.getBaseContext(), R.color.mainGold));
        Drawable drawable = ContextCompat.getDrawable(activity.getBaseContext(), R.drawable.plot_background);
        lineDataSet.setFillDrawable(drawable);
    }

    public void updateDatabase(ArrayList<MarketObject> trendingItems) {
        this.trendingItems = trendingItems;
        notifyDataSetChanged();
    }
}
