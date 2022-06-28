package com.arsinex.com;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public class WebSocketListener extends okhttp3.WebSocketListener {

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        webSocket.close(code, null);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        output("Error : " + t.getCause());
    }

    private void output(String msg){
        Log.d("**************", msg);
    }
}
