package com.arsinex.com.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class ConnectivityCheck {

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        try {
            boolean transportInfoWIFI = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            boolean transportInfoCELL = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            if (transportInfoCELL || transportInfoWIFI) { return true; } else { return false;}
        } catch (NullPointerException e) {
            return false;
        }
    }

}
