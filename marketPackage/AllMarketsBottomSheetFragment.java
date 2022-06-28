package com.arsinex.com.marketPackage;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketFragments;
import com.arsinex.com.enums.RequestType;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.RequestBody;

public class AllMarketsBottomSheetFragment extends BottomSheetDialogFragment implements AllMarketsListAdaptor.OnItemClickListener {

    private static final String MARKET_TAG = "market_list";

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private static final int CHART_MARKET_INTERVAL_DAY = 1800; // every half an hour

    // Initialize variables
    private TextView lblNoAnswer;
    private EditText txtCoinSearch;
    private RecyclerView recycleAllMarketList;
    private Dialog pleaseWaitDialog;

    private LinkedHashMap<String, MarketObject> marketsDictionary = new LinkedHashMap<String, MarketObject>();
    private ArrayList<MarketObject> marketsList = new ArrayList<MarketObject>();
    private ArrayList<MarketObject> marketsList_filtered = new ArrayList<MarketObject>();

    private AllMarketsListAdaptor allMarketsListAdaptor;
    private Boolean filter_status = false;

    private Utils utils = new Utils();
    private Gson gson = new Gson();

    private MarketViewModel marketViewModel;

    public static AllMarketsBottomSheetFragment newInstance() {
        return new AllMarketsBottomSheetFragment();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View marketView = inflater.inflate(R.layout.fragment_all_markets, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        return marketView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //Assign Variables
        lblNoAnswer = (TextView) view.findViewById(R.id.lblNoAnswer);
        txtCoinSearch = (EditText) view.findViewById(R.id.txtSearch);
        recycleAllMarketList = (RecyclerView) view.findViewById(R.id.recycleAllMarketList);

        txtCoinSearch.setEnabled(false);    // initially it is disabled till page is loaded completely

        // market recycle view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleAllMarketList.setLayoutManager(layoutManager);
        recycleAllMarketList.setItemAnimator(new DefaultItemAnimator());
        allMarketsListAdaptor = new AllMarketsListAdaptor(getActivity(), marketsList_filtered, this);
        recycleAllMarketList.setAdapter(allMarketsListAdaptor);

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
                allMarketsListAdaptor.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listenForComingDataFromServer();
    }

    @Override
    public void onAllMarketItemClick(MarketObject item) {
        marketViewModel.setMarket(item);
        marketViewModel.setFragment(MarketFragments.EXCHANGE);
        this.dismiss();
    }

    private void listenForComingDataFromServer() {
        // listens for market socket data
        marketViewModel.getSocketResponse().observe(getViewLifecycleOwner(), marketData -> {
            if(marketsDictionary.isEmpty()) { return; }
            try {
                if(!(new JSONObject(marketData).has("error"))) {
                    if (new JSONObject(marketData).getString("method").equals("price.update")) {
                        String moneyKey = updateMarketsDictionary(marketData);
                        updateMarketsListPrice(moneyKey);
                        updateMarketsAdaptor(moneyKey);
                        if(!txtCoinSearch.isEnabled()) { txtCoinSearch.setEnabled(true); }
                    }
                }
                if (new JSONObject(marketData).has("result") && new JSONObject(marketData).get("result") instanceof JSONArray) {
                    int updated_market_index = updateMarketChartInfo(marketData);
                    updateChartsView(updated_market_index);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false);
        });

        // listens for api response data
        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            if(requestType == RequestType.MARKET_LIST) {
                try {
                    if (marketsList != null) { marketsList.clear(); }
                    marketsList = parseMarketList(response);
                    saveMarketsIntoPreferences(marketsList);
                    createMarketsDictionary(marketsList);
                    fetchMarketsInfo();
                    fetchMarketsChartInfo(); // fetches information for chart (kline)
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false);
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            RequestType requestType = failurePair.second;

            if(requestType == RequestType.MARKET_LIST) {
                getMarketList();
            }

            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false);
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

    private ArrayList<MarketObject> loadMarketsFromPreferences() {
        String jsonString = utils.getFromSharedPreferences(getActivity(), "market_dict");

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
    private void saveMarketsIntoPreferences(ArrayList<MarketObject> list){ // TODO MAIN ACTIVITY SHOULD DO IT
        // getting data from gson and storing it in a string.
        String stringJSON = gson.toJson(list);
        utils.saveInSharedPreferences(getActivity(), "market_dict", stringJSON);
    }

    private void getMarketList(){
        final String url = new RequestUrls().getUrl(RequestType.MARKET_LIST);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(MARKET_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.MARKET_LIST);
    }
    private void createMarketsDictionary(ArrayList<MarketObject> list) {
        marketsDictionary.clear();
        for (MarketObject market: list) {
            marketsDictionary.put(market.getName(), market);
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

        marketViewModel.setSocketRequest(jsonRequest.toString());
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

            marketViewModel.setSocketRequest(jsonRequest.toString());
        }
    }


    private String updateMarketsDictionary(String response) throws JSONException, NullPointerException {
        String moneyName = new JSONObject(response).getJSONArray("params").getString(0);
        String price = new JSONObject(response).getJSONArray("params").getString(1);
        if (!marketsDictionary.containsKey(moneyName)) {
            Log.d("***************", moneyName);
        }
        marketsDictionary.get(moneyName).setPrice(price);
        return moneyName;
    }
    private void updateMarketsListPrice(String moneyName) {
        int index = new ArrayList<String>(marketsDictionary.keySet()).indexOf(moneyName);
        marketsList.get(index).setPrice(marketsDictionary.get(moneyName).getPrice());
    }
    private void updateMarketsAdaptor(String moneyName){
        marketsList_filtered.clear();

        if(filter_status) {
            filterResult(txtCoinSearch.getText());
        } else {
            marketsList_filtered.addAll(marketsList);
        }

        // if the arraylist in the adaptor is empty the whole list will be updated
        if(allMarketsListAdaptor.getItemCount() == 0) {
            allMarketsListAdaptor.updateDatabase(marketsList_filtered);
        } else {
            allMarketsListAdaptor.notifyDataSetChanged();
        }
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
    private void updateChartsView(int updated_market_index) {
        allMarketsListAdaptor.notifyItemChanged(updated_market_index);
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
    public void onResume() {
        super.onResume();

        // show waiting bar
        marketViewModel.setShowWaitingBar(true);

        // fetch data
        marketsList.clear();
        marketsList = loadMarketsFromPreferences();

        if (marketsList != null){
            try {
                createMarketsDictionary(marketsList);
                fetchMarketsInfo();
                fetchMarketsChartInfo(); // fetches information for chart (kline)
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            getMarketList();
        }
    }
}
