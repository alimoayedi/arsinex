package com.arsinex.com.Exchange;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arsinex.com.marketPackage.MarketViewModel;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.MarketOrderObject;
import com.arsinex.com.Objects.SharedMarketObject;
import com.arsinex.com.R;
import com.arsinex.com.enums.MarketAction;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderBooksFragment extends Fragment implements MarketOrdersAdaptor.OnItemClickListener {

    private RecyclerView listBuyOrders, listSellOrders;
    private ArrayList<MarketOrderObject> buyOrdersList = new ArrayList<MarketOrderObject>();
    private ArrayList<MarketOrderObject> sellOrdersList = new ArrayList<MarketOrderObject>();
    private MarketOrdersAdaptor buyOrdersAdaptor;
    private MarketOrdersAdaptor sellOrdersAdaptor;

    private SharedMarketObject sharedMarketObject = new SharedMarketObject();

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_orders_book, container, false);

        sharedMarketObject.setMarket(new GsonBuilder().create().fromJson(getArguments().getString("market"), MarketObject.class));

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        listBuyOrders = (RecyclerView) rootView.findViewById(R.id.listBuyOrders);
        listSellOrders = (RecyclerView) rootView.findViewById(R.id.listSellOrders);

        LinearLayoutManager layoutManager_buyOrders = new LinearLayoutManager(rootView.getContext());
        layoutManager_buyOrders.setOrientation(LinearLayoutManager.VERTICAL);
        listBuyOrders.setLayoutManager(layoutManager_buyOrders);
        listBuyOrders.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager_sellOrders = new LinearLayoutManager(rootView.getContext());
        layoutManager_sellOrders.setOrientation(LinearLayoutManager.VERTICAL);
        listSellOrders.setLayoutManager(layoutManager_sellOrders);
        listSellOrders.setItemAnimator(new DefaultItemAnimator());

        buyOrdersAdaptor = new MarketOrdersAdaptor(this.getContext(), buyOrdersList, MarketAction.BUY, this);
        sellOrdersAdaptor = new MarketOrdersAdaptor(this.getContext(), sellOrdersList, MarketAction.SELL, this);

        listBuyOrders.setAdapter(buyOrdersAdaptor);
        listSellOrders.setAdapter(sellOrdersAdaptor);

        listenForComingDataFromServer();

        return rootView;
    }

    private void listenForComingDataFromServer() {
        // listens for market socket data
        marketViewModel.getSocketResponse().observe(getViewLifecycleOwner(), response -> {
            try {
                updateOrdersBook(new JSONObject(response));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchMarketDeals() throws JSONException {
        JSONArray marketJSONArray = new JSONArray()
                .put(sharedMarketObject.getMarket().getName())
                .put(100) // limit
                .put("0"); // interval

        JSONObject jsonRequest = new JSONObject()
                    .put("id", 1)
                    .put("method", "depth.subscribe")
                    .put("params", marketJSONArray);

        marketViewModel.setSocketRequest(jsonRequest.toString());
    }

    private void updateOrdersBook(@NotNull JSONObject jsonResponse) throws JSONException {
        if(jsonResponse.has("error")) {
            return;
        }
        if (jsonResponse.getString("method").equals("depth.update")) {
            JSONObject orders = jsonResponse.getJSONArray("params").getJSONObject(1);

            if (orders.has("asks")) { // checks if any buy order exists
                buyOrdersList.clear(); // clears previous list before adding new orders
                JSONArray buyOrdersArray = orders.getJSONArray("asks"); //buy
                for (int INDEX = 0; INDEX < buyOrdersArray.length(); INDEX++) {
                    JSONArray jsonEntry = buyOrdersArray.getJSONArray(INDEX);
                    updateBuyOrders(jsonEntry);
                }
            }

            if (orders.has("bids")) { // checks if any sell order exists
                sellOrdersList.clear(); // clears previous orders before adding new orders
                JSONArray sellOrderArray = orders.getJSONArray("bids"); //sell
                for (int INDEX = 0; INDEX < sellOrderArray.length(); INDEX++) {
                    JSONArray jsonEntry = sellOrderArray.getJSONArray(INDEX);
                    updateSellOrders(jsonEntry);
                }
            }

            buyOrdersList.sort(new OrdersBookSorter(true)); // sorts buy orders in the ascending format
            sellOrdersList.sort(new OrdersBookSorter(false)); // sorts sell orders in the descending format

            buyOrdersAdaptor.notifyDataSetChanged(); // updates view
            sellOrdersAdaptor.notifyDataSetChanged(); // updates view
        }
    }

    private void updateBuyOrders(@NotNull JSONArray entry) throws JSONException {
        if (entry.getDouble(1) != 0) { // adds entry only if the amount is not equal zero
            buyOrdersList.add(new MarketOrderObject(
                    entry.getString(0), // price
                    entry.getString(1), // amount
                    null,
                    null
            ));
        }
    }
    private void updateSellOrders(@NotNull JSONArray entry) throws JSONException {
        if (entry.getDouble(1) != 0) { // adds only if the amount is not equal zero
            sellOrdersList.add(new MarketOrderObject(
                    entry.getString(0), // price
                    entry.getString(1), // amount
                    null,
                    null
            ));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        buyOrdersList.clear();
        sellOrdersList.clear();
        try {
            fetchMarketDeals();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOrderBookItemClick(MarketAction action, MarketOrderObject orderObject) {
        marketViewModel.setMarketOrder(action, orderObject);
    }
}
