package com.arsinex.com.Exchange;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.marketPackage.MarketViewModel;
import com.arsinex.com.Objects.MarketOrderObject;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.SharedMarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketAction;
import com.arsinex.com.enums.OrderType;
import com.arsinex.com.enums.Percentage;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.marketPackage.SharedMarket;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.RequestBody;

public class ExchangeFragment extends Fragment {

    private static final String TAG = "****************** Exchange Fragment *****************";

    private static final String BALANCE_TAG = "fetch_balance";

    private static final float PRICE_CHANGE_UNIT = (float) 0.1;
    private static final float AMOUNT_CHANGE_UNIT = (float) 0.01;

    private static final int CHART_MARKET_INTERVAL_HOUR = 1; // seconds
    private static final int CHART_MARKET_INTERVAL_DAY = 1800; // seconds
    private static final int CHART_MARKET_INTERVAL_WEEK = 1; // seconds

    private static final int HOUR_IN_SECONDS = 1;
    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private static final int WEEK_IN_SECONDS = 1;

    private static final int MA_HOUR_LAG = 1;
    private static final int MA_DAY_LAG = 15 * 60 * 60;
    private static final int MA_WEEK_LAG = 1;

    private static final int MA5 = 5;
    private static final int MA10 = 10;
    private static final int MA30 = 30;

    private static final int MA_LARGEST_LAG = 30;

    private static final String TIME_INTERVAL_PATTERN = "HH:mm";

    private TabLayout tabOrdersTabs;
    private TextView lblMarketSymbol, lbl_24H_High_value, lblPrice, lbl_24H_Low_value, lblChangeRate, lbl_24H_vol_value, lbl_24H_vol_unit, lblBalance, lblBalanceError;
    private ImageView imgArrow, imgMagnifier;
    private RelativeLayout lyMagnifier;
    private CombinedChart chartCombined;
    private BarChart chartBar;
    private Button btnBuy, btnSell;

    private boolean quantity_under_edit = false;
    private boolean total_under_edit = false;
    private OrderType orderType = OrderType.MARKET; // default is limit

    private OrderBooksFragment orderBooksFragment = new OrderBooksFragment();
    private OrdersHistoryFragment ordersHistoryFragment = new OrdersHistoryFragment();
    private OpenOrdersFragment openOrdersFragment =  new OpenOrdersFragment();

    // array of used data in charts
    private ArrayList<CandleEntry> candleChartDataPoints = new ArrayList<CandleEntry>();
    private ArrayList<BarEntry> barChartDataPoints = new ArrayList<BarEntry>();
    private ArrayList<Entry> lineChartDataPoints_MA5 = new ArrayList<Entry>();
    private ArrayList<Entry> lineChartDataPoints_MA10 = new ArrayList<Entry>();
    private ArrayList<Entry> lineChartDataPoints_MA30 = new ArrayList<Entry>();

    private ArrayList<String> chartXAxisLabels = new ArrayList<String>();
    private ArrayList<Float> closingPrices = new ArrayList<Float>();

    private HashMap<String, String> marketDictionary = new HashMap<String, String>();

    private Utils utils = new Utils();

    // chart magnifier transition
    private final Transition magnifier_transition = new Fade();

    // get selected market from parent activity (market activity)
    private SharedMarket sharedMarket = new SharedMarket();

    // make order view
    private View makeOrderWindow = null;
    private ExchangeUtilities exchangeUtilities = null;

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View exchangeView = inflater.inflate(R.layout.fragment_exchange, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        // gets market from market activity
        sharedMarket.setMarket(new GsonBuilder().create().fromJson(getArguments().getString("market"), MarketObject.class));

        // set toolbar color
        marketViewModel.setToolbarColor(ContextCompat.getColor(exchangeView.getContext(), R.color.transparent));

        exchangeUtilities = new ExchangeUtilities(getActivity(), sharedMarket.getMarket());

        return exchangeView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        uiComponentsInitialization(view);
        setMagnifierTransition();
        uiToLoadingStatus();
        orderTabLayoutSetup();

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSellBuyWindow(MarketAction.BUY, null);
            }
        });

        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSellBuyWindow(MarketAction.SELL, null);
            }
        });

        tabOrdersTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setOrdersFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        lyMagnifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgMagnifier.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(lyMagnifier, magnifier_transition);
                    imgMagnifier.setVisibility(View.VISIBLE);
                }
            }
        });

        imgMagnifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent exchangeFragmentLandscape = new Intent(getActivity(), ExchangeLandscape.class);
                exchangeFragmentLandscape.putExtra("market", sharedMarket.getMarket().getName());
                startActivity(exchangeFragmentLandscape);
            }
        });

        listenForComingDataFromServer();
    }

    private void uiComponentsInitialization(View exchangeView) {
        // price section components
        lblMarketSymbol = (TextView) exchangeView.findViewById(R.id.lblMarketSymbol);
        lbl_24H_High_value = (TextView) exchangeView.findViewById(R.id.lbl_24H_High_value);
        lblPrice = (TextView) exchangeView.findViewById(R.id.lblPrice);
        lbl_24H_Low_value = (TextView) exchangeView.findViewById(R.id.lbl_24H_Low_value);
        lblChangeRate = (TextView) exchangeView.findViewById(R.id.lblChangeRate);
        lbl_24H_vol_value = (TextView) exchangeView.findViewById(R.id.lbl_24H_vol_value);
        lbl_24H_vol_unit = (TextView) exchangeView.findViewById(R.id.lbl_24H_vol_unit);
        imgArrow = (ImageView) exchangeView.findViewById(R.id.imgArrow);

        // chart magnifier
        imgMagnifier = (ImageView) exchangeView.findViewById(R.id.imgMagnifier);
        lyMagnifier = (RelativeLayout) exchangeView.findViewById(R.id.lyMagnifier);

        // order section tabs
        tabOrdersTabs = (TabLayout) exchangeView.findViewById(R.id.tabOrdersTabs);

        // charts
        chartBar = (BarChart) exchangeView.findViewById(R.id.chartBar);
        chartCombined = (CombinedChart) exchangeView.findViewById(R.id.chartCombined);

        // buy and sell buttons
        btnBuy = (Button) exchangeView.findViewById(R.id.btnBuy);
        btnSell = (Button) exchangeView.findViewById(R.id.btnSell);
    }
    private void orderTabLayoutSetup() {
        tabOrdersTabs.addTab(tabOrdersTabs.newTab().setText(R.string.order_book));
        tabOrdersTabs.addTab(tabOrdersTabs.newTab().setText(R.string.history));
        tabOrdersTabs.addTab(tabOrdersTabs.newTab().setText(R.string.open_orders));
        tabOrdersTabs.setTabGravity(TabLayout.GRAVITY_FILL);
    }
    private void uiToLoadingStatus() {
//        lyFirstRow.setVisibility(View.INVISIBLE);
//        lySecondRow.setVisibility(View.INVISIBLE);
//        lyThirdRow.setVisibility(View.INVISIBLE);
//        chartBar.setVisibility(View.INVISIBLE);
//        chartCombined.setVisibility(View.INVISIBLE);
    }

    private void fetchMarketInfo() throws JSONException {
        JSONArray marketJSONArray = new JSONArray().put(sharedMarket.getMarket().getName());
        JSONObject jsonRequest = new JSONObject()
                .put("method", "state.subscribe")
                .put("params", marketJSONArray)
                .put("id", 1);

        marketViewModel.setSocketRequest(jsonRequest.toString());
    }
    private void fetchChartInfo() throws JSONException {
        int currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        JSONArray marketJSONArray = new JSONArray()
                .put(sharedMarket.getMarket().getName()) // market name
                .put(currentTime - DAY_IN_SECONDS - MA_DAY_LAG) // start time
                .put(currentTime)
                .put(CHART_MARKET_INTERVAL_DAY);
        JSONObject jsonRequest = new JSONObject()
                .put("method", "kline.query")
                .put("params", marketJSONArray)
                .put("id", 1);

        marketViewModel.setSocketRequest(jsonRequest.toString());
    }

    private void listenForComingDataFromServer() {
        // listens for market socket data
        marketViewModel.getSocketResponse().observe(getViewLifecycleOwner(), response -> {
            try {
                if(!(new JSONObject(response).has("error"))) {
                    if (new JSONObject(response).getString("method").equals("state.update")) {
                        updateMarketDictionary(response);
                        updateMarketView();
                    }
                }
                if ( new JSONObject(response).has("result") && new JSONObject(response).get("result") instanceof JSONArray) {
                    updateChartData(response);
                    updateChartsView();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            marketViewModel.setShowWaitingBar(false);
        });

        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            // extracts data
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            switch (requestType) {
                case PUT_LIMIT_ORDER:
                case PUT_MARKET_ORDER:
                    try {
                        exchangeUtilities.parseBuySellOrder(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case USER_BALANCE:
                    // checks if response is for balance and order windows is visible
                    if (makeOrderWindow != null && makeOrderWindow.getVisibility() == View.VISIBLE) {
                        updateUserBalanceView(response);
                    }
                    break;
            }
            marketViewModel.setShowWaitingBar(false);
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            RequestType requestType = failurePair.second;

            switch (requestType) {
                case PUT_LIMIT_ORDER:
                case PUT_MARKET_ORDER:
                    marketViewModel.setShowWaitingBar(false);
                    Toast.makeText(getContext(), getActivity().getResources().getString(R.string.failed_to_set_your_order), Toast.LENGTH_SHORT).show();
                    break;
                case USER_BALANCE:
                    getUserBalance();
                    break;
            }
        });

        marketViewModel.getMarketOrder().observe(getViewLifecycleOwner(), marketOrderPair -> {
            MarketAction action = marketOrderPair.first;
            MarketOrderObject orderObject = marketOrderPair.second;
            openSellBuyWindow(action, orderObject);
        });
    }

    private void updateMarketDictionary(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        marketDictionary.put("moneyName", jsonResponse.getJSONArray("params").getString(0));
        marketDictionary.put("day_high", jsonResponse.getJSONArray("params").getJSONObject(1).getString("high"));
        marketDictionary.put("day_low", jsonResponse.getJSONArray("params").getJSONObject(1).getString("low"));
        marketDictionary.put("day_vol", jsonResponse.getJSONArray("params").getJSONObject(1).getString("volume"));
        marketDictionary.put("day_open", jsonResponse.getJSONArray("params").getJSONObject(1).getString("open"));
        marketDictionary.put("price", jsonResponse.getJSONArray("params").getJSONObject(1).getString("last"));
    }

    private void updateChartData(String response) throws JSONException {
        JSONArray resultArray = new JSONObject(response).getJSONArray("result");
        clearChartDataSet();

        // Take the first 30 closing prices to to calculate Moving Averages
        for(int INDEX = 0; INDEX < MA_LARGEST_LAG; INDEX++) {
            closingPrices.add(Float.valueOf(resultArray.getJSONArray(INDEX).get(2).toString()));
        }

        // Starts from index 30. The first 30 items used for Moving Average calculation
        for (int INDEX = MA_LARGEST_LAG ; INDEX < resultArray.length() ; INDEX++) {
            candleChartDataPoints.add(new CandleEntry(
                    INDEX - MA_LARGEST_LAG, // x axis
                    Float.valueOf(resultArray.getJSONArray(INDEX).get(3).toString()), // shadow high
                    Float.valueOf(resultArray.getJSONArray(INDEX).get(4).toString()), // shadow low
                    Float.valueOf(resultArray.getJSONArray(INDEX).get(1).toString()), // open
                    Float.valueOf(resultArray.getJSONArray(INDEX).get(2).toString()) // close
            ));

            // NOTE: INDEX - MA_LARGEST_LAG shifts index to 0 inside the chart
            int chart_index = INDEX - MA_LARGEST_LAG;
            barChartDataPoints.add(new BarEntry(chart_index, Float.valueOf(resultArray.getJSONArray(INDEX).get(5).toString())));
            closingPrices.add(Float.valueOf(resultArray.getJSONArray(INDEX).get(2).toString()));
            lineChartDataPoints_MA5.add(new Entry(chart_index, getMovingAverage(INDEX, MA5)));
            lineChartDataPoints_MA10.add(new Entry(chart_index, getMovingAverage(INDEX, MA10)));
            lineChartDataPoints_MA30.add(new Entry(chart_index, getMovingAverage(INDEX, MA30)));
            chartXAxisLabels.add(new Utils().timeStampToHumanTime(Double.valueOf(resultArray.getJSONArray(chart_index).get(0).toString()), TIME_INTERVAL_PATTERN)); // X Axis Label
        }
    }

    private void clearChartDataSet() {
        candleChartDataPoints.clear();
        lineChartDataPoints_MA5.clear();
        lineChartDataPoints_MA10.clear();
        lineChartDataPoints_MA30.clear();
        barChartDataPoints.clear();
        closingPrices.clear();
        chartXAxisLabels.clear();
    }

    private float getMovingAverage(int position, int lag){
        float summation = 0;
        for(int INDEX=position-lag ; INDEX < position ; INDEX++){
            summation = summation + closingPrices.get(INDEX);
        }
        return summation / lag;
    }

    private void updateMarketView() {
        lblMarketSymbol.setText(sharedMarket.getMarket().getMarketSymbol());
        lbl_24H_High_value.setText(utils.reduceDecimal(marketDictionary.get("day_high"), Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        lblPrice.setText(utils.reduceDecimal(marketDictionary.get("price"),Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        lbl_24H_Low_value.setText(utils.reduceDecimal(marketDictionary.get("day_low"),Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        double priceChange = (Double.parseDouble(marketDictionary.get("price")) - Double.parseDouble(marketDictionary.get("day_open")))/100;
        if (priceChange < 0) {
            lblChangeRate.setTextColor(ContextCompat.getColor(getContext(), R.color.red_1));
            imgArrow.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_decrease_arrow));
        } else {
            lblChangeRate.setTextColor(ContextCompat.getColor(getContext(), R.color.green_1));
            imgArrow.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_increase_arrow));
        }
        lblChangeRate.setText(utils.reduceDecimal(String.valueOf(priceChange),Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        lbl_24H_vol_value.setText(utils.reduceDecimal(marketDictionary.get("day_vol"),Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        lbl_24H_vol_unit.setText(sharedMarket.getMarket().getStock());

//        if(pleaseWaitDialog.isShowing()) {
//            lyFirstRow.setVisibility(View.VISIBLE);
//            lySecondRow.setVisibility(View.VISIBLE);
//            lyThirdRow.setVisibility(View.VISIBLE);
//            chartBar.setVisibility(View.VISIBLE);
//            chartCombined.setVisibility(View.VISIBLE);
//            pleaseWaitDialog.dismiss();
//        }
    }

    private void updateChartsView() {
        setCombinedChart();
        setBarChart();
    }

    private void setCombinedChart() {
        chartCombined.getDescription().setEnabled(false);  // Hide the description
        chartCombined.getLegend().setEnabled(true);   // Hide the legend
        chartCombined.setMinOffset(0f); // no white margin on sides
        chartCombined.setExtraBottomOffset(2);
        chartCombined.setDrawBorders(false); // no border around chart
        chartCombined.setTouchEnabled(false);
        chartCombined.setScaleEnabled(false);
        chartCombined.setDragEnabled(false);
        chartCombined.setPinchZoom(false);
        chartCombined.setDoubleTapToZoomEnabled(false);
        chartCombined.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));

        // Add charts to the combined chart
        chartCombined.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.CANDLE,
                CombinedChart.DrawOrder.LINE
        });

        // set legend to the chart
        chartCombined.getLegend().setCustom(getLegend());
        chartCombined.getLegend().setTextColor(ContextCompat.getColor(getContext(), R.color.mainText));

        // set position of legend
        chartCombined.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        chartCombined.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        chartCombined.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        chartCombined.getLegend().setDrawInside(false);

        // set left Y axis
        chartCombined.getAxisLeft().setDrawLabels(false);
        chartCombined.getAxisLeft().setDrawAxisLine(false);
        chartCombined.getAxisLeft().setDrawZeroLine(false);
        chartCombined.getAxisLeft().setEnabled(false);
        chartCombined.getAxisLeft().setDrawGridLines(false);
        chartCombined.getAxisLeft().setSpaceTop(1);
        chartCombined.getAxisLeft().setDrawTopYLabelEntry(false);
        chartCombined.getAxisLeft().setDrawGridLines(false);

        // set right Y axis
        chartCombined.getAxisRight().setEnabled(true);
        chartCombined.getAxisRight().setDrawLabels(true); // right Y axis
        chartCombined.getAxisRight().setDrawAxisLine(false);
        chartCombined.getAxisRight().setDrawZeroLine(false);
        chartCombined.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chartCombined.getAxisRight().setTextColor(ContextCompat.getColor(getContext(), R.color.hintColor));
        chartCombined.getAxisRight().setSpaceTop(1);
        chartCombined.getAxisRight().setDrawTopYLabelEntry(false);
        chartCombined.getAxisRight().setDrawGridLines(false);

        // set X axis
        chartCombined.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartCombined.getXAxis().setDrawGridLines(false);
        chartCombined.getXAxis().setGridColor(ContextCompat.getColor(getContext(), R.color.grid));
        chartCombined.getXAxis().setTextColor(ContextCompat.getColor(getContext(), R.color.hintColor));
        chartCombined.getXAxis().setLabelCount(10);
        chartCombined.getXAxis().setValueFormatter(new IndexAxisValueFormatter(chartXAxisLabels));

        CombinedData combinedData = new CombinedData();
        combinedData.setData(getCandleData());  // add candle data
        combinedData.setData(getLineData());    // add moving averages

        chartCombined.setData(combinedData); // set charts data
        chartCombined.invalidate(); // update chart
    }

    private LegendEntry[] getLegend() {
        // set legend
        LegendEntry legendMA5=new LegendEntry( // Moving Average of 5
                "MA5",
                Legend.LegendForm.SQUARE,
                10f,
                2f,
                null,
                ContextCompat.getColor(getContext(), R.color.moving_average_5));

        LegendEntry legendMA10=new LegendEntry( // Moving Average of 10
                "MA10",
                Legend.LegendForm.SQUARE,
                10f,
                2f,
                null,
                ContextCompat.getColor(getContext(), R.color.moving_average_10));

        LegendEntry legendMA30=new LegendEntry( // Moving Average of 30
                "MA30",
                Legend.LegendForm.SQUARE,
                10f,
                2f,
                null,
                ContextCompat.getColor(getContext(), R.color.moving_average_30));

        return new LegendEntry[]{legendMA5, legendMA10, legendMA30};
    }

    private CandleData getCandleData(){
        CandleDataSet candleDataSet = new CandleDataSet(candleChartDataPoints, null);
        candleDataSet.setDrawValues(false);
        candleDataSet.setDrawIcons(false);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        candleDataSet.setShadowWidth(0.5f);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        candleDataSet.setDecreasingColor(ContextCompat.getColor(getContext(), R.color.negative_decrease));
        candleDataSet.setIncreasingColor(ContextCompat.getColor(getContext(), R.color.positive_increase));
        candleDataSet.setNeutralColor(ContextCompat.getColor(getContext(), R.color.positive_increase));
        candleDataSet.setShadowColorSameAsCandle(true);
        return new CandleData(candleDataSet);
    }

    private LineData getLineData() {
        ArrayList<ILineDataSet> lineDataSet_array = new ArrayList<ILineDataSet> ();

        LineDataSet lineDataSet_MA5 = new LineDataSet(lineChartDataPoints_MA5, "MA5");
        lineDataSet_MA5.setDrawValues(false);
        lineDataSet_MA5.setDrawIcons(false);
        lineDataSet_MA5.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet_MA5.setDrawCircles(false);
        lineDataSet_MA5.setColor(ContextCompat.getColor(getContext(), R.color.moving_average_5));
        lineDataSet_array.add(lineDataSet_MA5);

        LineDataSet lineDataSet_MA10 = new LineDataSet(lineChartDataPoints_MA10, "MA10");
        lineDataSet_MA10.setDrawValues(false);
        lineDataSet_MA10.setDrawIcons(false);
        lineDataSet_MA10.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet_MA10.setDrawCircles(false);
        lineDataSet_MA10.setColor(ContextCompat.getColor(getContext(), R.color.moving_average_10));
        lineDataSet_array.add(lineDataSet_MA10);

        LineDataSet lineDataSet_MA30 = new LineDataSet(lineChartDataPoints_MA30, "MA30");
        lineDataSet_MA30.setDrawValues(false);
        lineDataSet_MA30.setDrawIcons(false);
        lineDataSet_MA30.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet_MA30.setDrawCircles(false);
        lineDataSet_MA30.setColor(ContextCompat.getColor(getContext(), R.color.moving_average_30));
        lineDataSet_array.add(lineDataSet_MA30);

        return new LineData(lineDataSet_array);
    }

    private void setBarChart() {
        CustomBarChartDataset barDataSet = new CustomBarChartDataset(barChartDataPoints, "barChartData");
        barDataSet.setDrawValues(false);
        barDataSet.setDrawIcons(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        barDataSet.setColors(new int[]{
                ContextCompat.getColor(getContext(), R.color.positive_increase),
                ContextCompat.getColor(getContext(), R.color.negative_decrease)
        });

        // General chart
        chartBar.getDescription().setEnabled(false);  // Hide the description
        chartBar.getDescription().setPosition(2f,2f);
        chartBar.getLegend().setEnabled(false);   // Hide the legend
        chartBar.setMinOffset(0f);
        chartBar.setDrawBorders(false);

        // Left Y axis
        chartBar.getAxisLeft().setDrawLabels(false);
        chartBar.getAxisLeft().setDrawAxisLine(false);
        chartBar.getAxisLeft().setDrawZeroLine(false);
        chartBar.getAxisLeft().setEnabled(false);
        chartBar.getAxisLeft().setDrawGridLines(false);
        chartBar.getAxisLeft().setSpaceTop(0);
        chartBar.getAxisLeft().setDrawTopYLabelEntry(false);

        // Right Y axis
        chartBar.getAxisRight().setEnabled(true);
        chartBar.getAxisRight().setDrawLabels(true); // right Y axis
        chartBar.getAxisRight().setDrawAxisLine(false);
        chartBar.getAxisRight().setDrawZeroLine(false);
        chartBar.getAxisRight().setLabelCount(4);
        chartBar.getAxisRight().setGridColor(ContextCompat.getColor(getContext(), R.color.grid));
        chartBar.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chartBar.getAxisRight().setTextColor(ContextCompat.getColor(getContext(), R.color.hintColor));
        chartBar.getAxisRight().setSpaceTop(0);
        chartBar.getAxisRight().setDrawTopYLabelEntry(false);

        // X axis
        chartBar.getXAxis().setDrawLabels(false); // everything about X axis
        chartBar.getXAxis().setEnabled(false); // vertical gide lines
        chartBar.getXAxis().setGridColor(ContextCompat.getColor(getContext(), R.color.grid));

        BarData barData = new BarData(barDataSet);
        chartBar.setData(barData);
        chartBar.invalidate(); //update chart
    }

    private void setOrdersFragment(int elementID) {
        Bundle bundle = new Bundle();
        bundle.putString("market", new GsonBuilder().create().toJson(sharedMarket.getMarket()));
        switch (elementID){
            case 0:
                orderBooksFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentOrders, orderBooksFragment).commit();
                break;
            case 1:
                ordersHistoryFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentOrders, ordersHistoryFragment).commit();
                break;
            case 2:
                openOrdersFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentOrders, openOrdersFragment).commit();
                break;
        }
    }

    private void setMagnifierTransition() {
        magnifier_transition.setDuration(700);
        magnifier_transition.addTarget(imgMagnifier);
        magnifier_transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TransitionManager.beginDelayedTransition(lyMagnifier, transition);
                        imgMagnifier.setVisibility(View.GONE);
                    }
                }, 2000);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    private void openSellBuyWindow(MarketAction action, MarketOrderObject availableOrder){
        // make order window
        makeOrderWindow = LayoutInflater.from(getContext()).inflate(R.layout.layout_make_order, (LinearLayout) getActivity().findViewById(R.id.buySellWindow));
        makeOrderWindow.setTag(action);

        RelativeLayout lyPrice = (RelativeLayout)  makeOrderWindow.findViewById(R.id.lyPrice);
        LinearLayout lyOrderType = (LinearLayout) makeOrderWindow.findViewById(R.id.lyOrderType);
        TextView lblWindowTitle = (TextView) makeOrderWindow.findViewById(R.id.lblWindowTitle);
        TextView lblBalance = (TextView) makeOrderWindow.findViewById(R.id.lblBalance);
        TextView lblBalanceUnit = (TextView) makeOrderWindow.findViewById(R.id.lblBalanceUnit);
        TextView lblPair = (TextView) makeOrderWindow.findViewById(R.id.lblPair);
        TextView lblLimit = (TextView) makeOrderWindow.findViewById(R.id.lblLimit);
        TextView lblMarket = (TextView) makeOrderWindow.findViewById(R.id.lblMarket);
        ImageView imgPlusP = (ImageView) makeOrderWindow.findViewById(R.id.imgPlusP);
        ImageView imgMinusP = (ImageView) makeOrderWindow.findViewById(R.id.imgMinusP);
        EditText txtPrice = (EditText) makeOrderWindow.findViewById(R.id.txtPrice);
        ImageView imgPlusQ = (ImageView) makeOrderWindow.findViewById(R.id.imgPlusQ);
        ImageView imgMinusQ = (ImageView) makeOrderWindow.findViewById(R.id.imgMinusQ);
        EditText txtQuantity = (EditText) makeOrderWindow.findViewById(R.id.txtQuantity);
        EditText txtTotal = (EditText) makeOrderWindow.findViewById(R.id.txtTotal);
        TabLayout tabShareSelection = (TabLayout) makeOrderWindow.findViewById(R.id.tabShareSelection);
        Button btnAction = (Button) makeOrderWindow.findViewById(R.id.btnAction);
        View indicator25 = (View) makeOrderWindow.findViewById(R.id.indicator25);
        View indicator50 = (View) makeOrderWindow.findViewById(R.id.indicator50);
        View indicator75 = (View) makeOrderWindow.findViewById(R.id.indicator75);
        View indicator100 = (View) makeOrderWindow.findViewById(R.id.indicator100);

        // make percentage tabs disabled until balance is fetched
        percentageTabsActivation(false,tabShareSelection);

        // Fetch balance
        getUserBalance();

        //Set bottom window title (sell or buy) and set the money unit accordingly
        if (action == MarketAction.BUY) {
            lblWindowTitle.setText(getResources().getString(R.string.buy));
            lblBalanceUnit.setText(sharedMarket.getMarket().getMoney());
        } else {
            lblWindowTitle.setText(getResources().getString(R.string.sell));
            lblBalanceUnit.setText(sharedMarket.getMarket().getStock());
        }

        // set market name
        lblPair.setText(sharedMarket.getMarket().getMarketSymbol());

        // sets the price field. Gets value from the exchange main fragment.
        txtPrice.setText(lblPrice.getText());

        // defines what should the button do!
        setupActionButton(btnAction, action);

        lblLimit.setOnClickListener(v -> {
            setOrderType(OrderType.LIMIT, lblLimit, lblMarket);
            lyPrice.setVisibility(View.VISIBLE);
            lyOrderType.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.binary_toggle_background_right));

        });

        lblMarket.setOnClickListener(v -> {
            setOrderType(OrderType.MARKET, lblLimit, lblMarket);
            lyPrice.setVisibility(View.GONE);
            lyOrderType.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.binary_toggle_background_left));

            // set price to the market real current price
            txtPrice.setText(lblPrice.getText());
        });

        // set order type and set price field as GONE
        lblMarket.performClick();

        // controls changes in the available balance label. When fetched share tabs gets active.
        lblBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                percentageTabsActivation(true, tabShareSelection);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // price increase btn
        imgPlusP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets current price if field is empty
                if(txtPrice.getText().toString().equals("")){
                    txtPrice.setText(lblPrice.getText().toString());
                }

                //gets focus if not focused and moves cursor to the end of string
                if (!txtPrice.hasFocus()) {
                    txtPrice.requestFocus();
                    txtPrice.setSelection(txtPrice.getText().length());
                }

                // calculates add to the price
                // because of turkish language "," is replaced with "." if exists
                String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());

                float newValue = Float.parseFloat(price) + PRICE_CHANGE_UNIT;
                if (newValue >= 0){
                    txtPrice.setText(utils.reduceDecimal(String.valueOf(newValue),2));
                } else {
                    txtPrice.setText("0.0");
                }
            }
        });

        // price decrease btn
        imgMinusP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets current price if field is empty
                if(txtPrice.getText().toString().equals("")){
                    txtPrice.setText(lblPrice.getText().toString());
                }

                //gets focus if not focused and moves cursor to the end of string
                if (!txtPrice.hasFocus()) {
                    txtPrice.requestFocus();
                    txtPrice.setSelection(txtPrice.getText().length());
                }

                // calculates subtract from the price
                // because of turkish language "," is replaced with "." if exists
                String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());

                float newValue = Float.parseFloat(price) - PRICE_CHANGE_UNIT;
                if (newValue >= 0){
                    txtPrice.setText(utils.reduceDecimal(String.valueOf(newValue),2));
                } else {
                    txtPrice.setText("0.0");
                }
            }
        });

        // price edittext field
        txtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // if user initially enters "." without any number
                if(charSequence.toString().equals(".")) {
                    txtPrice.setText("0" + txtPrice.getText());
                    txtPrice.setSelection(txtPrice.getText().length());
                }


                // replaces "," with "." in turkish for float numbers
                String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());
                String quantity = utils.standardizeTurkishStrings(txtQuantity.getText().toString());

                total_under_edit = true;
                exchangeUtilities.calculateTotal(price, quantity, txtTotal);
                total_under_edit = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //quantity increase field
        imgPlusQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets focus if not focused and moves cursor to the end of string
                if (!txtQuantity.hasFocus()) {
                    txtQuantity.requestFocus();
                    txtQuantity.setSelection(txtQuantity.getText().length());
                }

                // checks if input is empty or not number (Nan)
                if(txtQuantity.getText().toString().equals("") || !txtQuantity.getText().toString().matches("\\d+(?:\\.\\d+)?")){
                    txtQuantity.setText("0.0");
                }

                // checks for "," to replace with "." in turkish language
                String quantity = utils.standardizeTurkishStrings(txtQuantity.getText().toString());

                float newValue = Float.parseFloat(quantity) + AMOUNT_CHANGE_UNIT;
                if (newValue >= 0) {
                    txtQuantity.setText(utils.reduceDecimal(String.valueOf(newValue), 2));
                } else {
                    txtQuantity.setText("0.0");
                }
            }
        });

        //quantity decrease field
        imgMinusQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets focus if not focused and moves cursor to the end of string
                if (!txtQuantity.hasFocus()) {
                    txtQuantity.requestFocus();
                    txtQuantity.setSelection(txtQuantity.getText().length());
                }

                // checks if input is empty or not number (Nan)
                if(txtQuantity.getText().toString().equals("") || !txtQuantity.getText().toString().matches("\\d+(?:\\.\\d+)?")){
                    txtQuantity.setText("0.0");
                }

                // checks for "," to replace with "." in turkish language
                String quantity = utils.standardizeTurkishStrings(txtQuantity.getText().toString());


                float newValue = Float.parseFloat(quantity) - AMOUNT_CHANGE_UNIT;
                if (newValue >= 0) {
                    txtQuantity.setText(utils.reduceDecimal(String.valueOf(newValue), 2));
                } else {
                    txtQuantity.setText("0.0");
                }
            }
        });

        //quantity edittext field
        txtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (txtQuantity.hasFocus()) {
                    quantity_under_edit = true;
                }

                // if user initially enters "." without any number
                if(charSequence.toString().equals(".")) {
                    txtQuantity.setText("0" + txtQuantity.getText());
                    txtQuantity.setSelection(txtQuantity.getText().length());
                }

                // if total field has focus it is under edition so need to edit and change it
                if(!total_under_edit) {
                    // replaces "," with "." in turkish for float numbers
                    String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());
                    String quantity = utils.standardizeTurkishStrings(txtQuantity.getText().toString());
                    exchangeUtilities.calculateTotal(price, quantity, txtTotal);
                }
                quantity_under_edit = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (txtTotal.hasFocus()) {
                    total_under_edit = true;
                }

                // if user initially enters "." without any number
                if(charSequence.toString().equals(".")) {
                    txtTotal.setText("0" + txtTotal.getText());
                    txtTotal.setSelection(txtTotal.getText().length());
                }

                // if total field in not empty and has focus.
                if (!quantity_under_edit) {
                    // replaces "," with "." in turkish for float numbers
                    String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());
                    String total = utils.standardizeTurkishStrings(txtTotal.getText().toString());
                    exchangeUtilities.calculateQuantity(price, total, txtQuantity);
                }
                total_under_edit = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tabShareSelection.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setPercentageIndicator(tab.getPosition(), indicator25, indicator50, indicator75, indicator100);

                quantity_under_edit = true;

                // replaces "," with "." in turkish for float numbers
                String balance = utils.standardizeTurkishStrings(lblBalance.getText().toString());
                String price = utils.standardizeTurkishStrings(marketDictionary.get("price"));

                if (!balance.isEmpty()) {
                    switch (tab.getPosition()) {
                        case 0:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.QUARTER));
                            break;
                        case 1:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.HALF));
                            break;
                        case 2:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.TRI_QUARTER));
                            break;
                        case 3:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.FULL));
                            break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setPercentageIndicator(tab.getPosition(), indicator25, indicator50, indicator75, indicator100);

                quantity_under_edit = true;

                // replaces "," with "." in turkish for float numbers
                String balance = utils.standardizeTurkishStrings(lblBalance.getText().toString());
                String price = utils.standardizeTurkishStrings(marketDictionary.get("price"));

                if (!balance.isEmpty()) {
                    switch (tab.getPosition()) {
                        case 0:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.QUARTER));
                            break;
                        case 1:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.HALF));
                            break;
                        case 2:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.TRI_QUARTER));
                            break;
                        case 3:
                            txtQuantity.setText(exchangeUtilities.calculateFraction(action, balance, price, Percentage.FULL));
                            break;
                    }
                }
            }
        });

        btnAction.setOnClickListener(v -> {
            // replaces "," with "." in turkish for float numbers
            String balance = utils.standardizeTurkishStrings(lblBalance.getText().toString());
            String price = utils.standardizeTurkishStrings(txtPrice.getText().toString());
            String quantity = utils.standardizeTurkishStrings(txtQuantity.getText().toString());
            String total = utils.standardizeTurkishStrings(txtTotal.getText().toString());

            if (action == MarketAction.BUY && exchangeUtilities.buyErrorCheck(orderType, balance, price, quantity, total)) {
                setOrderRequest(MarketAction.BUY, orderType, sharedMarket.getMarket().getName(), quantity, price);
            } else if (action == MarketAction.SELL && exchangeUtilities.sellErrorCheck(orderType, balance, price, quantity)) {
                setOrderRequest(MarketAction.SELL, orderType, sharedMarket.getMarket().getName(), quantity, price);
            }
        });

        final BottomSheetDialog buySellDialog = new BottomSheetDialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        buySellDialog.setContentView(makeOrderWindow);
        buySellDialog.setCancelable(true);
        buySellDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

        // cancels any ongoing request to API
        buySellDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO cancel requests!
                // exchangeUtilities.cancelFetches(BALANCE_TAG);
            }
        });

        // checks if user selected an already available order
        if (availableOrder != null) {
            lblLimit.performClick();
            txtPrice.setText(availableOrder.getPrice());
            quantity_under_edit = true;
            txtQuantity.setText(availableOrder.getAmount());
        }

        buySellDialog.show();
    }

    private void getUserBalance() {
        String requestURL = new RequestUrls().getUrl(RequestType.USER_BALANCE);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(BALANCE_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.USER_BALANCE);
    }

    private void updateUserBalanceView(String response) {
        MarketAction action = (MarketAction) makeOrderWindow.getTag();

        HashMap<String, String> balance = exchangeUtilities.parseBalanceResponse(response);
        ProgressBar progressLoading = (ProgressBar) makeOrderWindow.findViewById(R.id.progressLoading);
        TextView lblBalance = (TextView) makeOrderWindow.findViewById(R.id.lblBalance);
        TextView lblBalanceError = (TextView) makeOrderWindow.findViewById(R.id.lblBalanceError);

        // dismiss loading progress bar
        progressLoading.setVisibility(View.GONE);

        // updates balance view accordingly
        if (balance.equals(null)) {
            lblBalanceError.setVisibility(View.VISIBLE);
        } else if (action == MarketAction.BUY) {
            lblBalance.setText(utils.reduceDecimal(balance.get(sharedMarket.getMarket().getMoney()), Integer.parseInt(sharedMarket.getMarket().getMoney_prec())));
        } else if (action == MarketAction.SELL) {
            lblBalance.setText(utils.reduceDecimal(balance.get(sharedMarket.getMarket().getStock()), Integer.parseInt(sharedMarket.getMarket().getStock_prec())));
        }
    }

    private void setOrderRequest(MarketAction action, OrderType orderType, String market, String amount, String price) {

        marketViewModel.setShowWaitingBar(true);

        Request request;
        if (orderType == OrderType.LIMIT) {
            request = exchangeUtilities.generateLimitRequest(action, market, amount, price);
            marketViewModel.setApiRequest(request, RequestType.PUT_LIMIT_ORDER);
        } else if (orderType == OrderType.MARKET) {
            request = exchangeUtilities.generateMarketRequest(action, market, amount);
            marketViewModel.setApiRequest(request, RequestType.PUT_MARKET_ORDER);
        }
    }

    private void setOrderType(OrderType type, TextView lblLimit, TextView lblMarket) {
        // saves the order type
        orderType = type;

        // update view
        if (type == OrderType.LIMIT) {
            lblLimit.setTypeface(null, Typeface.BOLD);
            lblMarket.setTypeface(null, Typeface.NORMAL);
        } else if (type == OrderType.MARKET){
            lblLimit.setTypeface(null, Typeface.NORMAL);
            lblMarket.setTypeface(null, Typeface.BOLD);
        }
    }

    /**
     *
     * @param enabled
     * @param tabLayout
     */
    private void percentageTabsActivation(boolean enabled, TabLayout tabLayout) {
        tabLayout.getTabAt(0).view.setClickable(enabled);
        tabLayout.getTabAt(1).view.setClickable(enabled);
        tabLayout.getTabAt(2).view.setClickable(enabled);
        tabLayout.getTabAt(3).view.setClickable(enabled);
    }

    private void setPercentageIndicator(int selectedTabIndex, View indicator25, View indicator50, View indicator75, View indicator100) {
        switch (selectedTabIndex) {
            case 0:
                indicator25.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_selected_tab));
                indicator50.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator75.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                break;
            case 1:
                indicator25.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator50.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_selected_tab));
                indicator75.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                break;
            case 2:
                indicator25.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator50.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator75.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_selected_tab));
                indicator100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                break;
            case 3:
                indicator25.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator50.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator75.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_unselected_tab));
                indicator100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.order_percentage_selected_tab));
                break;
        }
    }

    private void setupActionButton(Button btnAction, MarketAction action) {
        switch (action) {
            case BUY:
                btnAction.setText(getResources().getString(R.string.buy));
                btnAction.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_1));
                break;
            case SELL:
                btnAction.setText(getResources().getString(R.string.sell));
                btnAction.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red_1));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //show the waiting spinner bar
        marketViewModel.setShowWaitingBar(true);

        // send request to server and fetch data
        try {
            fetchMarketInfo();
            fetchChartInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // activate the first fragment in the orders sections
        setOrdersFragment(0);
    }

}