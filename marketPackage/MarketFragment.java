package com.arsinex.com.marketPackage;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arsinex.com.APIResponseParser;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketFragments;
import com.arsinex.com.enums.RequestType;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MarketFragment extends Fragment implements MarketsListAdaptor.OnItemClickListener, TrendMarketsAdaptor.OnItemClickListener {

    private static final String TAG = "****************** Market Fragment *****************";
    private static final String USER_TAG = "user";
    private static final String MARKET_TAG = "market_list";
    private static final String BALANCE_TAG = "balance";
    private static final String ALL_MARKET_TAG = "all_market";

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    private static final int CHART_MARKET_INTERVAL_DAY = 1800; // every half an hour

    private NestedScrollView nestedScrollView;
    private RecyclerView recycleTrend, recycleMarketList;
    private TextView lblName,lblBalanceValue, lblTrending, lblSeeAllMarket;

    private LinkedHashMap<String, MarketObject> marketsDictionary = new LinkedHashMap<String, MarketObject>();
    private ArrayList<MarketObject> marketsList = new ArrayList<MarketObject>();

    private MarketsListAdaptor marketsListAdaptor;
    private TrendMarketsAdaptor trendMarketsAdaptor;

    private final OkHttpClient client = new OkHttpClient();

    private Gson gson = new Gson();
    private Utils utils = new Utils();

    private AllMarketsBottomSheetFragment allMarketsBottomSheetFragment = new AllMarketsBottomSheetFragment();

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View marketView = inflater.inflate(R.layout.fragment_market, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        return marketView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        lblName = (TextView) view.findViewById(R.id.lblName);
        recycleTrend = (RecyclerView) view.findViewById(R.id.recycleTrendMarkets);
        recycleMarketList = (RecyclerView) view.findViewById(R.id.recycleMarketList);
        lblSeeAllMarket = (TextView) view.findViewById(R.id.lblAllMarket);
        lblTrending = (TextView) view.findViewById(R.id.lblTrending);
        lblBalanceValue = (TextView) view.findViewById(R.id.lblBalanceValue);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.nestedScrollView);

        lblSeeAllMarket.setClickable(false);

        // trend recycle view
        LinearLayoutManager layoutManager_trend = new CustomLinearLayoutManager(view.getContext());
        layoutManager_trend.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleTrend.setLayoutManager(layoutManager_trend);
        recycleTrend.setItemAnimator(new DefaultItemAnimator());

        // market recycle view
        LinearLayoutManager layoutManager_topMarket = new LinearLayoutManager(view.getContext());
        layoutManager_topMarket.setOrientation(LinearLayoutManager.VERTICAL);
        recycleMarketList.setLayoutManager(layoutManager_topMarket);
        recycleMarketList.setItemAnimator(new DefaultItemAnimator());

        // initialize adaptors
        marketsListAdaptor = new MarketsListAdaptor(this.getActivity(), marketsList, this);
        trendMarketsAdaptor = new TrendMarketsAdaptor(this.getActivity(), marketsList, this);

        // assign adaptors
        recycleMarketList.setAdapter(marketsListAdaptor);
        recycleTrend.setAdapter(trendMarketsAdaptor);

        lblSeeAllMarket.setOnClickListener(new View.OnClickListener() { // TODO needs update changes to a dialog comes up from button
            @Override
            public void onClick(View view) {
                allMarketsBottomSheetFragment.show(getActivity().getSupportFragmentManager(), ALL_MARKET_TAG);
            }
        });

        nestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY == 0) {
                    marketViewModel.setToolbarColor(ContextCompat.getColor(getContext(), R.color.transparent));
                } else if (scrollY < 513) {
                    marketViewModel.setToolbarColor(Color.argb((int) (scrollY * .5), 112, 112, 112));
                } else{
                    marketViewModel.setToolbarColor(ContextCompat.getColor(getContext(), R.color.market_header_mask));
                }
            }
        });

        getUserInfo();
        listenForComingDataFromServer();

    }

    @Override
    public void onMarketItemClick(MarketObject market) {
        marketViewModel.setMarket(market);
        marketViewModel.setFragment(MarketFragments.EXCHANGE);
    }

    @Override
    public void onTrendItemClick(MarketObject market) {
        marketViewModel.setMarket(market);
        marketViewModel.setFragment(MarketFragments.EXCHANGE);
    }

    private void listenForComingDataFromServer() {
        // listens for market socket data
        marketViewModel.getSocketResponse().observe(getViewLifecycleOwner(), marketData -> {
            try {
                if(!(new JSONObject(marketData).has("error"))) {
                    if (new JSONObject(marketData).getString("method").equals("price.update")) {
                        String moneyKey = updateMarketsDictionary(marketData);
                        updateMarketsListPrice(moneyKey);
                        updateMarketsAdaptor(moneyKey);
                    }
                }
                if (new JSONObject(marketData).has("result") && new JSONObject(marketData).get("result") instanceof JSONArray) {
                    int updated_market_index = updateMarketChartInfo(marketData);
                    updateChartsView(updated_market_index);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // listens for api response data
        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            switch (requestType) {
                case USER:
                    try {
                        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(requestType, response);
                        lblName.setText(hashResponse.get("name").toString() + " " + hashResponse.get("surname").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case WALLET_BALANCE:
                    try {
                        parseBalanceResponse(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case MARKET_LIST:
                    try {
                        if (marketsList != null) { marketsList.clear(); }
                        marketsList = parseMarketList(response);
                        saveMarketsIntoPreferences(marketsList);
                        createMarketsDictionary(marketsList);
                        fetchMarketsInfo();
                        fetchMarketsChartInfo(); // fetches information for chart (kline)
                        lblSeeAllMarket.setClickable(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }

            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false);
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            RequestType requestType = failurePair.second;

            switch (requestType) {
                case USER:
                    getUserInfo();
                    break;
                case WALLET_BALANCE:
                    getUserWalletBalance();
                    break;
                case MARKET_LIST:
                    getMarketList();
                    break;
            }
        });
    }

    private void getUserInfo() {
        String url = new RequestUrls().getUrl(RequestType.USER);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(USER_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.USER);
    }
    private void getUserWalletBalance(){
        final String url = new RequestUrls().getUrl(RequestType.WALLET_BALANCE);
        final RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(BALANCE_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.WALLET_BALANCE);
    }
    private void getMarketList(){
        final String url = new RequestUrls().getUrl(RequestType.MARKET_LIST);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(MARKET_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.MARKET_LIST);
    }

    private void parseBalanceResponse(String response) throws JSONException {
        JSONObject jsonBalance = new JSONObject(response).getJSONObject("balances");
        setNetWorth(jsonBalance.getString("totalBalance"));
    }
    private void setNetWorth(String net_worth) {
        lblBalanceValue.setText(utils.reduceDecimal(net_worth,2) + " ₺");
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

    private void saveMarketsIntoPreferences(ArrayList<MarketObject> list){ // TODO MAIN ACTIVITY SHOULD DO IT
        // getting data from gson and storing it in a string.
        String stringJSON = gson.toJson(list);
        utils.saveInSharedPreferences(getActivity(), "market_dict", stringJSON);
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
        // if the arraylist in the adaptor is empty the whole list will be updated
        if (marketsListAdaptor.getItemCount() == 0) {
            trendMarketsAdaptor.updateDatabase(marketsList);
            marketsListAdaptor.updateDatabase(marketsList);
        } else {
            int index = new ArrayList<String>(marketsDictionary.keySet()).indexOf(moneyName);
            marketsListAdaptor.notifyItemChanged(index);
            trendMarketsAdaptor.notifyItemChanged(index);
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
        marketsListAdaptor.notifyItemChanged(updated_market_index);
        trendMarketsAdaptor.notifyItemChanged(updated_market_index);
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
    } // TODO should be moved to the main activity

    @Override
    public void onResume() {
        super.onResume();

        // fetch data
        getUserWalletBalance();
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

    @Override
    public void onPause() {
        if(allMarketsBottomSheetFragment.isVisible()) {
            allMarketsBottomSheetFragment.dismiss();
        }

        super.onPause();
    }
}