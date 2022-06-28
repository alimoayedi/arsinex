package com.arsinex.com.Exchange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.WebSocketListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class ExchangeLandscape extends AppCompatActivity {

    private static final String WEBSOCKET_TAG = "websocket_tag";

    private static final String TIME_INTERVAL_PATTERN = "HH:mm";

    private static final int CHART_MARKET_INTERVAL_HOUR = 1; // seconds
    private static final int CHART_MARKET_INTERVAL_DAY = 1800; // seconds
    private static final int CHART_MARKET_INTERVAL_WEEK = 1; // seconds

    private static final int HOUR_IN_SECONDS = 1;
    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private static final int WEEK_IN_SECONDS = 1;
    private static final int TWO_DAY_IN_SECONDS = 2 * 24 * 60 * 60;


    private static final int MA_HOUR_LAG = 1;
    private static final int MA_DAY_LAG = 15 * 60 * 60;
    private static final int MA_WEEK_LAG = 1;

    private static final int MA5 = 5;
    private static final int MA10 = 10;
    private static final int MA30 = 30;

    private static final int MA_LARGEST_LAG = 30;

    private ImageView btnBack;
    private TextView lblMarketName;

    private CombinedChart chartCombined;
    private BarChart chartBar;
    private Dialog pleaseWaitDialog;

    private String market;

    private ArrayList<CandleEntry> candleChartDataPoints = new ArrayList<CandleEntry>();
    private ArrayList<BarEntry> barChartDataPoints = new ArrayList<BarEntry>();
    private ArrayList<Entry> lineChartDataPoints_MA5 = new ArrayList<Entry>();
    private ArrayList<Entry> lineChartDataPoints_MA10 = new ArrayList<Entry>();
    private ArrayList<Entry> lineChartDataPoints_MA30 = new ArrayList<Entry>();

    private ArrayList<String> chartXAxisLabels = new ArrayList<String>();
    private ArrayList<Float> closingPrices = new ArrayList<Float>();

    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    private final Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_landscape);

        btnBack = (ImageView) findViewById(R.id.btnBack);
        lblMarketName = (TextView) findViewById(R.id.lblMarketName);
        chartCombined = (CombinedChart) findViewById(R.id.chartCombined);
        chartBar = (BarChart) findViewById(R.id.chartBar);

        chartCombined.setVisibility(View.INVISIBLE);
        chartBar.setVisibility(View.INVISIBLE);

        pleaseWaitDialog = new Dialog(this);
        utils.setupDialog(pleaseWaitDialog, this);
        pleaseWaitDialog.show();

        setConnectionTimeout();

        market = getIntent().getExtras().getString("market");

        btnBack.setOnClickListener(view -> {
            onBackPressed();
        });

        lblMarketName.setText(market);
    }

    private void setConnectionTimeout() {
        client.newBuilder()
                .connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private void connectToSocket() {
        Request request = new Request.Builder().url("wss://wss.arsinex.com:8443/").tag(WEBSOCKET_TAG).build();
        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String response) {
                if (this != null && !isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if ( new JSONObject(response).has("result") && new JSONObject(response).get("result") instanceof JSONArray) {
                                    updateChartData(response);
                                    updateChartsView();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };
        webSocket = client.newWebSocket(request, webSocketListener);
    }

    private void fetchChartInfo() throws JSONException {
        int currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        JSONArray marketJSONArray = new JSONArray()
                .put(market) // market name
                .put(currentTime - TWO_DAY_IN_SECONDS - MA_DAY_LAG) // start time
                .put(currentTime) // end time
                .put(CHART_MARKET_INTERVAL_DAY);
        JSONObject jsonRequest = new JSONObject()
                .put("method", "kline.query")
                .put("params", marketJSONArray)
                .put("id", 1);

        sendRequestToSocket(jsonRequest.toString());
    }

    private void sendRequestToSocket(String request) {
        webSocket.send(request);
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

    private void updateChartsView() {
        setCombinedChart();
        setBarChart();
        if(pleaseWaitDialog.isShowing()) {
            chartCombined.setVisibility(View.VISIBLE);
            chartBar.setVisibility(View.VISIBLE);
            pleaseWaitDialog.dismiss();
        }
    }

    private void setCombinedChart() {
        chartCombined.getDescription().setEnabled(false);  // Hide the description
        chartCombined.getLegend().setEnabled(true);   // Hide the legend
        chartCombined.setMinOffset(0f);
        chartCombined.setExtraBottomOffset(5);
        chartCombined.setDrawBorders(false);
        chartCombined.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        // Add charts to the combined chart
        chartCombined.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.CANDLE,
                CombinedChart.DrawOrder.LINE
        });

        // set legend to the chart
        chartCombined.getLegend().setCustom(getLegend());
        chartCombined.getLegend().setTextColor(ContextCompat.getColor(this, R.color.mainText));

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
        chartCombined.getAxisLeft().setSpaceTop(2);
        chartCombined.getAxisLeft().setSpaceBottom(2);
        chartCombined.getAxisLeft().setDrawTopYLabelEntry(true);
        chartCombined.getAxisLeft().setDrawGridLines(false);


        // set right Y axis
        chartCombined.getAxisRight().setEnabled(true);
        chartCombined.getAxisRight().setDrawLabels(true); // right Y axis
        chartCombined.getAxisRight().setDrawAxisLine(false);
        chartCombined.getAxisRight().setDrawZeroLine(false);
        chartCombined.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chartCombined.getAxisRight().setTextColor(ContextCompat.getColor(this, R.color.hintColor));
        chartCombined.getAxisRight().setSpaceTop(2);
        chartCombined.getAxisRight().setSpaceBottom(2);
        chartCombined.getAxisRight().setLabelCount(10);
        chartCombined.getAxisRight().setDrawTopYLabelEntry(true);
        chartCombined.getAxisRight().setDrawGridLines(false);

        // set X axis
        chartCombined.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartCombined.getXAxis().setDrawGridLines(false);
        chartCombined.getXAxis().setGridColor(ContextCompat.getColor(this, R.color.grid));
        chartCombined.getXAxis().setTextColor(ContextCompat.getColor(this, R.color.hintColor));
        chartCombined.getXAxis().setLabelCount(20);
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
                ContextCompat.getColor(this, R.color.moving_average_5));

        LegendEntry legendMA10=new LegendEntry( // Moving Average of 10
                "MA10",
                Legend.LegendForm.SQUARE,
                10f,
                2f,
                null,
                ContextCompat.getColor(this, R.color.moving_average_10));

        LegendEntry legendMA30=new LegendEntry( // Moving Average of 30
                "MA30",
                Legend.LegendForm.SQUARE,
                10f,
                2f,
                null,
                ContextCompat.getColor(this, R.color.moving_average_30));

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
        candleDataSet.setDecreasingColor(ContextCompat.getColor(this, R.color.negative_decrease));
        candleDataSet.setIncreasingColor(ContextCompat.getColor(this, R.color.positive_increase));
        candleDataSet.setNeutralColor(ContextCompat.getColor(this, R.color.positive_increase));
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
        lineDataSet_MA5.setColor(ContextCompat.getColor(this, R.color.moving_average_5));
        lineDataSet_array.add(lineDataSet_MA5);

        LineDataSet lineDataSet_MA10 = new LineDataSet(lineChartDataPoints_MA10, "MA10");
        lineDataSet_MA10.setDrawValues(false);
        lineDataSet_MA10.setDrawIcons(false);
        lineDataSet_MA10.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet_MA10.setDrawCircles(false);
        lineDataSet_MA10.setColor(ContextCompat.getColor(this, R.color.moving_average_10));
        lineDataSet_array.add(lineDataSet_MA10);

        LineDataSet lineDataSet_MA30 = new LineDataSet(lineChartDataPoints_MA30, "MA30");
        lineDataSet_MA30.setDrawValues(false);
        lineDataSet_MA30.setDrawIcons(false);
        lineDataSet_MA30.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet_MA30.setDrawCircles(false);
        lineDataSet_MA30.setColor(ContextCompat.getColor(this, R.color.moving_average_30));
        lineDataSet_array.add(lineDataSet_MA30);

        return new LineData(lineDataSet_array);
    }

    private void setBarChart() {
        CustomBarChartDataset barDataSet = new CustomBarChartDataset(barChartDataPoints, "barChartData");
        barDataSet.setDrawValues(false);
        barDataSet.setDrawIcons(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        barDataSet.setColors(new int[]{
                ContextCompat.getColor(this, R.color.positive_increase),
                ContextCompat.getColor(this, R.color.negative_decrease)
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
        chartBar.getAxisRight().setGridColor(ContextCompat.getColor(this, R.color.grid));
        chartBar.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chartBar.getAxisRight().setTextColor(ContextCompat.getColor(this, R.color.hintColor));
        chartBar.getAxisRight().setSpaceTop(0);
        chartBar.getAxisRight().setDrawTopYLabelEntry(false);

        // X axis
        chartBar.getXAxis().setDrawLabels(false); // everything about X axis
        chartBar.getXAxis().setEnabled(false); // vertical gide lines
        chartBar.getXAxis().setGridColor(ContextCompat.getColor(this, R.color.grid));

        BarData barData = new BarData(barDataSet);
        chartBar.setData(barData);
        chartBar.invalidate(); //update chart
    }

    private JSONObject getUnsubscribeRequest(String method) {
        JSONArray paramsArray = new JSONArray();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("method", method)
                    .put("params", paramsArray)
                    .put("id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonRequest;
    }

    @Override
    public void onBackPressed() {
        // closes connection to the web-socket
        webSocket.close(1000, null);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // connects to the web-socket again
        connectToSocket();

        // gets subscribes for the chart info
        try {
            fetchChartInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // unsubscribes from the web-socket
        sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString());
        super.onPause();
    }

    @Override
    protected void onStop() {
        // closes connection to the web socket on activity stop
        webSocket.close(1000, null);

        // changes orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onStop();
    }
}