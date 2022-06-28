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

import com.arsinex.com.FetchAndLoadImage;
import com.arsinex.com.Objects.MarketObject;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class AllMarketsListAdaptor extends RecyclerView.Adapter<AllMarketsListAdaptor.MyViewHolder> {

    private Activity activity;
    private ArrayList<MarketObject> marketsList;
    private OnItemClickListener onItemClickListener;

    private Utils utils = new Utils();

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView lblCurrencyName, lblCurrencySymbol, lblPrice, lblPriceUnit, lblPriceChange;
        private ImageView imgLogo;
        private LineChart chartLine;

        private OnItemClickListener onItemClickListener;

        MyViewHolder(@NonNull View view, OnItemClickListener onItemClickListener) {
            super(view);
            imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
            lblCurrencyName = (TextView) view.findViewById(R.id.lblCurrencyName);
            lblCurrencySymbol = (TextView) view.findViewById(R.id.lblCurrencySymbol);
            lblPrice = (TextView) view.findViewById(R.id.lblPrice);
            lblPriceUnit = (TextView) view.findViewById(R.id.lblPriceUnit);
            lblPriceChange = view.findViewById(R.id.lbl_change_24h_rate);
            chartLine = (LineChart) view.findViewById(R.id.chartLine);
            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onAllMarketItemClick(marketsList.get(getLayoutPosition()));
        }
    }

    public interface OnItemClickListener {
        void onAllMarketItemClick(MarketObject item);
    }

    public AllMarketsListAdaptor(Activity activity, ArrayList<MarketObject> marketsList, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.marketsList = marketsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_market_list_item, parent, false);
        return new MyViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        ArrayList<Entry> dataPoints = new ArrayList<Entry>();
        ArrayList<ILineDataSet> chartDataset = new ArrayList<>();
        MarketObject market = marketsList.get(position);
        holder.lblCurrencyName.setText(market.getStock());
        holder.lblCurrencySymbol.setText(market.getStock());
        holder.lblPrice.setText(utils.reduceDecimal(market.getPrice(), Integer.parseInt(market.getMoney_prec())));
        holder.lblPriceUnit.setText(market.getMoney());


//        double priceChange = Double.parseDouble(market.getPriceChange());
//        if (priceChange < 0) {
//            holder.lblPriceChange.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.error_red));
//        } else {
//            holder.lblPriceChange.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.green_1));
//        }
//        holder.lblPriceChange.setText(utils.reduceDecimal(market.getPriceChange(),2));

        setChartViewProperties(holder.chartLine); // set chart view properties

        LineDataSet lineDataSet = new LineDataSet(market.getChartData(), "lineChart");
        setGraphProperties(lineDataSet); // set properties of graph

        LineData lineData = new LineData(lineDataSet);

        holder.chartLine.setData(lineData);
        holder.chartLine.invalidate();

        if(!market.getStock().equals("null")) {
            FetchAndLoadImage fetchAndLoadImage = new FetchAndLoadImage(activity.getApplicationContext());
            fetchAndLoadImage.setImage(market.getStock(), holder.imgLogo, "ic_" + market.getStock());
        }
    }

    @Override
    public int getItemCount() {
        return marketsList.size();
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
        Drawable drawable = ContextCompat.getDrawable(activity.getBaseContext(), R.color.transparent);
        lineDataSet.setFillDrawable(drawable);
    }

    public void updateDatabase(ArrayList<MarketObject> marketsList) {
        this.marketsList = marketsList;
    }

}
