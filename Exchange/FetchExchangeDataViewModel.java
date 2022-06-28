package com.arsinex.com.Exchange;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.arsinex.com.GetURL;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FetchExchangeDataViewModel extends ViewModel {

    private GetURL getURL = new GetURL();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private MutableLiveData<String> data_market;

    private final OkHttpClient client = new OkHttpClient();
    private Handler exchange_handler = new Handler();
    private Runnable makeRequest;
    private boolean ignoreResponse = false;

    private RequestBody requestBody = null;

    private int FETCH_DELAY = 10000;

    public MutableLiveData<String> getData_Exchange(int market_id){
        if (data_market == null) {
            data_market = new MutableLiveData<>();
        }
        this.ignoreResponse = ignoreResponse;
        fetchExchangeMarket(market_id);
        return data_market;
    }

//    public MutableLiveData<String> getData_orders(){
//        if (data_orders == null) {
//            data_orders = new MutableLiveData<>();
//            fetchTrend();
//        }
//        return data_orders;
//    }

    public void fetchExchangeMarket(int market_id) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("market", market_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
        Request request = new Request.Builder().url(getURL.get(GetURL.EXCHANGE_INFO)).post(requestBody).build();
        client.newBuilder().connectTimeout(120, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(120, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(120, TimeUnit.SECONDS);

        makeRequest = new Runnable() {
            @Override
            public void run() {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, final Response response) throws IOException {
                        if (!ignoreResponse) {
                            data_market.postValue(response.body().string());
                        }
                    }
                });
                if (!ignoreResponse) {
                    exchange_handler.postDelayed(this, FETCH_DELAY);
                }
            }
        };
        exchange_handler.post(makeRequest);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void cancelFetch(){
        exchange_handler.removeCallbacks(makeRequest);
        ignoreResponse = true;
    }
     public void setIgnoreResponse(boolean ignoreResponse){
        this.ignoreResponse = ignoreResponse;
     }
}
