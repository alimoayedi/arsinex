package com.arsinex.com.marketPackage;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.MarketOrderObject;
import com.arsinex.com.enums.MarketAction;
import com.arsinex.com.enums.MarketFragments;
import com.arsinex.com.enums.RequestType;

import okhttp3.Request;

public class MarketViewModel extends ViewModel {

    private final MutableLiveData<Integer> statusBarColor = new MutableLiveData<Integer>();
    private final MutableLiveData<Integer> toolbarColor = new MutableLiveData<Integer>();
    private final MutableLiveData<Boolean> showWaitingBar = new MutableLiveData<Boolean>();
    private final MutableLiveData<MarketObject> market = new MutableLiveData<MarketObject>();
    private final MutableLiveData<MarketFragments> fragment = new MutableLiveData<MarketFragments>();
    private final MutableLiveData<Pair<Request, RequestType>> apiRequest = new MutableLiveData<Pair<Request, RequestType>>();
    private final MutableLiveData<Pair<String, RequestType>> apiResponse = new MutableLiveData<Pair<String, RequestType>>();
    private final MutableLiveData<String> socketRequest = new MutableLiveData<String>();
    private final MutableLiveData<String> socketResponse = new MutableLiveData<String>();
    private final MutableLiveData<Pair<Boolean, RequestType>> requestFailed = new MutableLiveData<Pair<Boolean, RequestType>>();
    private final MutableLiveData<Pair<String, RequestType>> serverError = new MutableLiveData<Pair<String, RequestType>>();
    private final MutableLiveData<Pair<MarketAction, MarketOrderObject>> marketOrder = new MutableLiveData<Pair<MarketAction, MarketOrderObject>>();

    public void setStatusBarColor(int color) {
        this.statusBarColor.setValue(color);
    }
    public MutableLiveData<Integer> getStatusBarColor() {
        return statusBarColor;
    }

    public void setToolbarColor(int color) { this.toolbarColor.setValue(color); }
    public MutableLiveData<Integer> getToolbarColor() { return toolbarColor; }

    public void setShowWaitingBar(Boolean show) {
        this.showWaitingBar.setValue(show);
    }
    public MutableLiveData<Boolean> getShowWaitingBar() {
        return showWaitingBar;
    }

    public void setMarket(MarketObject market) { this.market.setValue(market);}
    public MutableLiveData<MarketObject> getMarket() { return market; }

    public void setFragment(MarketFragments fragment) {
        this.fragment.setValue(fragment);
    }
    public MutableLiveData<MarketFragments> getFragment() {
        return fragment;
    }

    public void setApiRequest(Request request, RequestType requestType) {
        this.apiRequest.setValue(new Pair<>(request, requestType));
    }
    public MutableLiveData<Pair<Request, RequestType>> getApiRequest() {
        return apiRequest;
    }

    public void setApiResponse(String response, RequestType requestType) {
        this.apiResponse.setValue(new Pair<>(response, requestType));
    }
    public MutableLiveData<Pair<String, RequestType>> getApiResponse() {
        return apiResponse;
    }

    public void setSocketRequest(String request) {
        this.socketRequest.setValue(request);
    }
    public MutableLiveData<String> getSocketRequest() {
        return socketRequest;
    }

    public void setSocketResponse(String fetchedData) {
        this.socketResponse.setValue(fetchedData);
    }
    public MutableLiveData<String> getSocketResponse() {
        return socketResponse;
    }

    public void setServerError(String error, RequestType requestType) {
        this.serverError.setValue(new Pair<String, RequestType>(error, requestType));
    }
    public MutableLiveData<Pair<String, RequestType>> getServerError() { return serverError; }

    public void setRequestFailed(boolean status, RequestType requestType) {
        this.requestFailed.setValue(new Pair<Boolean, RequestType>(status, requestType));
    }
    public MutableLiveData<Pair<Boolean, RequestType>> isRequestFailed() { return requestFailed; }

    public void setMarketOrder(MarketAction action, MarketOrderObject order) {
        this.marketOrder.setValue(new Pair<MarketAction, MarketOrderObject>(action, order));
    }
    public MutableLiveData<Pair<MarketAction, MarketOrderObject>> getMarketOrder() {
        return marketOrder;
    }

}
