package com.arsinex.com.marketPackage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.WebSocketListener;
import com.arsinex.com.enums.RequestType;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;

public class AllMarketActivity extends AppCompatActivity implements AllMarketsListAdaptor.OnItemClickListener {

    private static final String MARKET_TAG = "market_list";
    private static final String WEBSOCKET_TAG = "websocket_tag";

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private static final int CHART_MARKET_INTERVAL_DAY = 1800; // every half an hour

    // Initialize variables
    private ImageView btnBack;
    private TextView lblNoAnswer;
    private EditText txtCoinSearch;
    private RecyclerView recycleAllMarketList;
    private Dialog pleaseWaitDialog;

    private LinkedHashMap<String, MarketObject> marketsDictionary = new LinkedHashMap<String, MarketObject>();
    private ArrayList<MarketObject> marketsList = new ArrayList<MarketObject>();
    private ArrayList<MarketObject> marketsList_filtered = new ArrayList<MarketObject>();

    private AllMarketsListAdaptor marketsListAdaptor;
    private Boolean filter_status = false;

    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    private Utils utils = new Utils();
    private Gson gson = new Gson();
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_market);

        pleaseWaitDialog = new Dialog(this);
        utils.setupDialog(pleaseWaitDialog, this);
        pleaseWaitDialog.show();

        initializeActivity();
        setConnectionTimeout();

        // market recycle view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleAllMarketList.setLayoutManager(layoutManager);
        recycleAllMarketList.setItemAnimator(new DefaultItemAnimator());
        marketsListAdaptor = new AllMarketsListAdaptor(this, marketsList_filtered, this);

        txtCoinSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                marketsList_filtered.clear();
                if (charSequence.length() == 0){
                    filter_status = false;
                    marketsList_filtered.addAll(marketsList);
                } else {
                    filter_status = true;
                    filterResult(charSequence);
                }
                marketsListAdaptor.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initializeActivity() {
        //Assign Variables
        btnBack = (ImageView) findViewById(R.id.btnBack);
        lblNoAnswer = findViewById(R.id.lblNoAnswer);
        txtCoinSearch = (EditText) findViewById(R.id.txtSearch);
        recycleAllMarketList = (RecyclerView) findViewById(R.id.recycleAllMarketList);
        txtCoinSearch.setEnabled(false);    // initially it is disabled till page is loaded completely
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
                if(this != null && !isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(!(new JSONObject(response).has("error"))) {
                                    if (new JSONObject(response).getString("method").equals("price.update")) {
                                        String moneyKey = updateMarketsDictionary(response);
                                        updateMarketsList(moneyKey);
                                        updateMarketsList();
                                    }
                                }
                                if (new JSONObject(response).has("result") && new JSONObject(response).get("result") instanceof JSONArray) {
                                    int updated_market_index = updateMarketChartInfo(response);
                                    updateChartsView(updated_market_index);
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

    private ArrayList<MarketObject> loadMarketsFromPreferences() {

        String jsonString = utils.getFromSharedPreferences(this, "market_dict");

        // checking below if the saved list exists or not
        if (jsonString == null) {
            return null;
        } else {
            // determines the type of objects in the array list
            Type type = new TypeToken<ArrayList<MarketObject>>() {}.getType();
            // in below line we are getting data from gson and saving it to our array list
            ArrayList<MarketObject> marketsList = gson.fromJson(jsonString, type);
            return marketsList;
        }
    }

    private void getMarketList(){
        RequestBody requestBody = RequestBody.create(new byte[0]);
        String requestURL = new RequestUrls().getUrl(RequestType.MARKET_LIST);
        Request request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(MARKET_TAG).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                failureRequest();
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                if (response.code() == HttpsURLConnection.HTTP_OK) {
                    String stringResponse = response.body().string();
                    try {
                        marketsList = parseMarketList(stringResponse);
                        saveMarketsIntoPreferences(marketsList);
                        createMarketsDictionary(marketsList);
                        setMarketsList();
                        fetchMarketsInfo();
                        fetchMarketsChartInfo();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            failureResponse(response.code());
                        }
                    });
                }
                response.close();
            }
        });
    }

    private ArrayList<MarketObject> parseMarketList(String response) throws JSONException {
        ArrayList<MarketObject> list = new ArrayList<MarketObject>();
        JSONArray arrayResponse = new JSONArray(response);
        for(int INDEX=0; INDEX<arrayResponse.length(); INDEX++) {
            MarketObject marketObject = new MarketObject(
                    arrayResponse.getJSONObject(INDEX).getString("name"),
                    arrayResponse.getJSONObject(INDEX).getString("stock"),
                    arrayResponse.getJSONObject(INDEX).getString("money"),
                    arrayResponse.getJSONObject(INDEX).getString("money_prec"),
                    arrayResponse.getJSONObject(INDEX).getString("stock_prec")
            );
            list.add(marketObject);
        }
        return list;
    }

    private void saveMarketsIntoPreferences(ArrayList<MarketObject> list){
        // getting data from gson and storing it in a string.
        String stringJSON = gson.toJson(list);
        utils.saveInSharedPreferences(this, "market_dict", stringJSON);
    }

    private void createMarketsDictionary(ArrayList<MarketObject> list) {
        for (MarketObject market: list) {
            marketsDictionary.put(market.getName(), market);
        }
    }

    private void setMarketsList() {
        for(String key: marketsDictionary.keySet()) {
            marketsList.add(marketsDictionary.get(key));
        }
    }

    private void fetchMarketsInfo() throws JSONException {
        JSONArray marketJSONArray = new JSONArray();
        for (MarketObject market:marketsList) {
            marketJSONArray.put(market.getName());
        }
        final JSONObject jsonRequest = new JSONObject()
                .put("id", 1)
                .put("method", "price.subscribe")
                .put("params", marketJSONArray);

        sendRequestToSocket(jsonRequest.toString());
    }
    private void fetchMarketsChartInfo() throws JSONException {
        int currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        for (MarketObject market:marketsList) {
            final JSONArray paramsArray = new JSONArray()
                    .put(market.getName())
                    .put(currentTime - DAY_IN_SECONDS) // start time
                    .put(currentTime) // end time
                    .put(CHART_MARKET_INTERVAL_DAY);

            final JSONObject jsonRequest = new JSONObject()
                    .put("id", 1)
                    .put("method", "kline.query")
                    .put("params", paramsArray);

            sendRequestToSocket(jsonRequest.toString());
        }
    }

    private void sendRequestToSocket(String request) {
        webSocket.send(request);
    }

    private String updateMarketsDictionary(String response) throws JSONException {
        String moneyName = new JSONObject(response).getJSONArray("params").getString(0);
        String price = new JSONObject(response).getJSONArray("params").getString(1);
        marketsDictionary.get(moneyName).setPrice(price);
        return moneyName;
    }

    private void updateMarketsList(String moneyName) {
        int index = new ArrayList<String>(marketsDictionary.keySet()).indexOf(moneyName);
        marketsList.get(index).setPrice(marketsDictionary.get(moneyName).getPrice());
    }

    private int updateMarketChartInfo(String response) throws JSONException {
        ArrayList<Entry> lineChartDataPoints = new ArrayList<Entry>(); // Line chart data points

        JSONArray resultArray = new JSONObject(response).getJSONArray("result");

        for (int INDEX = 0 ; INDEX < resultArray.length() ; INDEX++) {
            // average of close and open in the period
            float avg = (Float.valueOf(resultArray.getJSONArray(INDEX).get(1).toString()) // open
                    + Float.valueOf(resultArray.getJSONArray(INDEX).get(2).toString())) / 2; // close
            lineChartDataPoints.add(new Entry(INDEX, avg));
        }
        int market_index = new ArrayList<String>(marketsDictionary.keySet()).indexOf(resultArray.getJSONArray(0).get(7).toString());
        marketsList.get(market_index).setChartData(lineChartDataPoints);
        return market_index;
    }

    private void updateMarketsList(){
        if(pleaseWaitDialog.isShowing()) {
            pleaseWaitDialog.dismiss();
            txtCoinSearch.setEnabled(true);
        }
        marketsList_filtered.clear();
        if(filter_status) {
            filterResult(txtCoinSearch.getText());
        } else {
            marketsList_filtered.addAll(marketsList);
        }
        if (recycleAllMarketList.getChildCount() == 0) {
            recycleAllMarketList.setAdapter(marketsListAdaptor);
        } else {
            marketsListAdaptor.notifyDataSetChanged();
        }
    }

    private void updateChartsView(int updated_market_index) {
        marketsListAdaptor.notifyItemChanged(updated_market_index);
    }

    private void filterResult(CharSequence charSequence){
        for (int index = 0; index< marketsList.size(); index++) {
            if (marketsList.get(index).getStock().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                marketsList_filtered.add(marketsList.get(index));
            }
        }
        if (marketsList_filtered.isEmpty()) {
            recycleAllMarketList.setVisibility(View.GONE);
            lblNoAnswer.setVisibility(View.VISIBLE);
        } else {
            recycleAllMarketList.setVisibility(View.VISIBLE);
            lblNoAnswer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAllMarketItemClick(MarketObject item) {
        Intent marketActivity = new Intent(AllMarketActivity.this, MarketActivity.class);
        marketActivity.putExtra("market", gson.toJson(item));
        marketActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        marketActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(marketActivity);
        finishAffinity();
    }

    private void failureRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
        btnNegative.setVisibility(View.GONE); // No need for this button here!
        lblMsgHeader.setText(getResources().getString(R.string.error));
        lblMsg.setText(getResources().getString(R.string.server_connection_failure));
        btnPositive.setText(getResources().getString(R.string.try_again));
        dialog.setCancelable(true);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getMarketList();
            }
        });
        dialog.show();
    }

    private void failureResponse (int http_code) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the custom layout
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        btnNegative.setVisibility(View.GONE); // No need for this button here!
        btnPositive.setText(getString(R.string.ok));

        if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED){
            lblMsgHeader.setText(getResources().getString(R.string.sessionTimeOut));
            lblMsg.setText(R.string.session_timeout_msg);
        } else {
            lblMsgHeader.setText(getResources().getString(R.string.error));
            lblMsg.setText(String.valueOf(http_code));
        }

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    Intent loginActivity = new Intent(AllMarketActivity.this, LoginRegisterActivity.class);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginActivity);
                    finishAffinity();
                }
            }
        });
        // show dialog
        dialog.show();
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

    private void cancelRequest(String fetchTag) {
        // cancel all queued calls
        for(Call call : client.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(fetchTag)) {
                call.cancel();
            }
        }

        //cancels all running calls
        for(Call call : client.dispatcher().runningCalls()) {
            if (call.request().tag().equals(fetchTag)) {
                call.cancel();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectToSocket();

        // if market list already exists
        ArrayList<MarketObject> list = loadMarketsFromPreferences();
        if (list != null){
            createMarketsDictionary(list);
            setMarketsList();
            try {
                fetchMarketsInfo();
                fetchMarketsChartInfo(); // fetches information for chart (kline)
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            getMarketList();
        }
    }

    @Override
    protected void onPause() {
        cancelRequest(MARKET_TAG);
        sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString());
        webSocket.close(1000, null);
        super.onPause();
    }


    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}