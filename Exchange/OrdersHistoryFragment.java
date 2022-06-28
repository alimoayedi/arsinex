package com.arsinex.com.Exchange;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arsinex.com.marketPackage.MarketViewModel;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.MarketOrderHistoryObject;
import com.arsinex.com.Objects.SharedMarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketAction;
import com.arsinex.com.enums.RequestType;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OrdersHistoryFragment extends Fragment {

    private static final String TAG = "***************** ORDER HISTORY *****************";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String ORDER_HISTORY_TAG = "history";

    private static final int HISTORY_LIMIT = 100;
    private static final int HISTORY_OFFSET = 0;
    private static final int DAY_IN_SECONDS = 24 * 60 * 60;

    private RecyclerView recycleOrdersHistory;
    private TextView lblNoRecord;
    private ProgressBar progressLoadingHistory;
    private ArrayList<MarketOrderHistoryObject> marketHistoryList = new ArrayList<MarketOrderHistoryObject>();
    private MarketHistoryAdaptor marketHistoryAdaptor;

    private SharedMarketObject sharedMarketObject = new SharedMarketObject();

    private Utils utils = new Utils();

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View historyView = inflater.inflate(R.layout.layout_orders_history, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        // gets market from market activity
        sharedMarketObject.setMarket(new GsonBuilder().create().fromJson(getArguments().getString("market"), MarketObject.class));

        recycleOrdersHistory = (RecyclerView) historyView.findViewById(R.id.recycleOrdersHistory);
        lblNoRecord = (TextView) historyView.findViewById(R.id.lblNoRecord);
        progressLoadingHistory = (ProgressBar) historyView.findViewById(R.id.progressLoadingHistory);

        LinearLayoutManager layoutManager_history = new LinearLayoutManager(historyView.getContext());
        layoutManager_history.setOrientation(LinearLayoutManager.VERTICAL);
        recycleOrdersHistory.setLayoutManager(layoutManager_history);
        recycleOrdersHistory.setItemAnimator(new DefaultItemAnimator());

        // doesn't show list to user and shows spinner
        recycleOrdersHistory.setVisibility(View.GONE);

        // sets adaptor
        marketHistoryAdaptor = new MarketHistoryAdaptor(this.getContext(), marketHistoryList);

        marketHistoryList.clear();

        uiToLoadingMode();

        listenForComingDataFromServer();

        fetchHistory(MarketAction.BUY);
        fetchHistory(MarketAction.SELL);

        return historyView;
    }

    private void uiToLoadingMode() {
        progressLoadingHistory.setVisibility(View.VISIBLE);
        recycleOrdersHistory.setVisibility(View.GONE);
        lblNoRecord.setVisibility(View.GONE);
    }

    private void listenForComingDataFromServer() {
        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            if (requestType == RequestType.ORDER_FINISHED) {
                try {
                    parseResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            fetchHistory(MarketAction.BUY);
            fetchHistory(MarketAction.SELL);
        });
    }

    private void fetchHistory(MarketAction action){
        // gets url
        final String url = new RequestUrls().getUrl(RequestType.ORDER_FINISHED);

        // gets current time
        int currentTime = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("market", sharedMarketObject.getMarket().getName())
                    .put("limit", HISTORY_LIMIT)
                    .put("offset", HISTORY_OFFSET)
                    .put("start_time", currentTime - DAY_IN_SECONDS)
                    .put("end_time", currentTime)
                    .put("side", action == MarketAction.SELL ? 1 : 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(ORDER_HISTORY_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.ORDER_FINISHED);
    }

    private void parseResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("error")) {
            progressLoadingHistory.setVisibility(View.GONE);
            recycleOrdersHistory.setVisibility(View.GONE);
            lblNoRecord.setText(R.string.not_authorized);
            lblNoRecord.setVisibility(View.VISIBLE);
            return;
        }
        updateHistoryOrdersList(jsonResponse);
    }

    private void updateHistoryOrdersList(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse.getJSONArray("records").length() > 0) {
            JSONArray ordersArray = jsonResponse.getJSONArray("records");
            for (int INDEX = 0; INDEX < ordersArray.length(); INDEX++) {
                JSONObject historyItem = ordersArray.getJSONObject(INDEX);
                marketHistoryList.add(new MarketOrderHistoryObject(
                        historyItem.getString("side"),
                        historyItem.getString("type"),
                        historyItem.getString("price"),
                        historyItem.getString("amount"),
                        historyItem.getDouble("ctime")
                ));
            }
            updateHistoryOrdersAdaptor();
        }
        if (marketHistoryList.size() == 0){
            recycleOrdersHistory.setVisibility(View.GONE);
            lblNoRecord.setVisibility(View.VISIBLE);
        }
        progressLoadingHistory.setVisibility(View.GONE);
    }
    private void updateHistoryOrdersAdaptor() {
        recycleOrdersHistory.setVisibility(View.VISIBLE);
        if (recycleOrdersHistory.getChildCount() == 0) {
            recycleOrdersHistory.setAdapter(marketHistoryAdaptor);
        } else {
            marketHistoryAdaptor.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}