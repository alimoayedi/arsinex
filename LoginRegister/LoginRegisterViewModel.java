package com.arsinex.com.LoginRegister;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arsinex.com.enums.LoginRegistrationFragments;
import com.arsinex.com.enums.RequestType;

import org.json.JSONObject;

public class LoginRegisterViewModel extends ViewModel {
    private final MutableLiveData<Boolean> showWaitingBar = new MutableLiveData<Boolean>();
    private final MutableLiveData<Boolean> requestResentStatus = new MutableLiveData<Boolean>();
    private final MutableLiveData<Pair<JSONObject, RequestType>> jsonRequest = new MutableLiveData<Pair<JSONObject, RequestType>>();
    private final MutableLiveData<Pair<String, RequestType>> apiResponse = new MutableLiveData<Pair<String, RequestType>>();
    private final MutableLiveData<Boolean> requestFailed = new MutableLiveData<Boolean>();
    private final MutableLiveData<RequestType> resendRequest = new MutableLiveData<RequestType>();
    private final MutableLiveData<LoginRegistrationFragments> fragment = new MutableLiveData<LoginRegistrationFragments>();
    private final MutableLiveData<RequestType> cancelPendingRequest = new MutableLiveData<RequestType>();

    public void setShowWaitingBar(Boolean show) {
        this.showWaitingBar.setValue(show);
    }
    public MutableLiveData<Boolean> getShowWaitingBar() {
        return showWaitingBar;
    }

    public void setRequestResentStatus(Boolean status) {
        this.requestResentStatus.setValue(status);
    }
    public MutableLiveData<Boolean> getRequestResentStatus() {
        return requestResentStatus;
    }

    public void setJsonRequest(JSONObject dataToPass, RequestType requestType) {
        this.jsonRequest.setValue(new Pair<>(dataToPass, requestType));
    }
    public MutableLiveData<Pair<JSONObject, RequestType>> getJsonRequest() {
        return jsonRequest;
    }

    public void setApiResponse(String response, RequestType requestType) {
        this.apiResponse.setValue(new Pair<>(response, requestType));
    }
    public MutableLiveData<Pair<String, RequestType>> getApiResponse() {
        return apiResponse;
    }

    public void setRequestFailed(boolean status) {
        requestFailed.setValue(status);
    }
    public MutableLiveData<Boolean> isRequestFailed() { return requestFailed; }

    public void setResendRequest(RequestType requestType) {
        resendRequest.setValue(requestType);
    }
    public MutableLiveData<RequestType> getResendRequest() {
        return resendRequest;
    }

    public void setFragment(LoginRegistrationFragments fragment) {
        this.fragment.setValue(fragment);
    }
    public MutableLiveData<LoginRegistrationFragments> getFragment(){
        return fragment;
    }

    public void setCancelPendingRequest(RequestType requestType) {
        cancelPendingRequest.setValue(requestType);
    }
    public MutableLiveData<RequestType> getCancelPendingRequest(){
        return cancelPendingRequest;
    }

}
