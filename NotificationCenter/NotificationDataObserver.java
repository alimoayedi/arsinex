package com.arsinex.com.NotificationCenter;

import com.arsinex.com.OnDataChangeListener;

public class NotificationDataObserver {
    private OnDataChangeListener listener;
    private String response;

    public void setOnChangeListener(OnDataChangeListener listener) {
        this.listener = listener;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        if (listener != null) {
            listener.onDataChanged(response);
        }
    }
}
