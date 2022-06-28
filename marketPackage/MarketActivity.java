package com.arsinex.com.marketPackage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.APIResponseParser;
import com.arsinex.com.Balance.BalanceFragment;
import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.Exchange.ExchangeFragment;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Predicition.PredictionFragment;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.SharedMarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.MainActivityUtilities;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.WebSocketListener;
import com.arsinex.com.enums.MarketFragments;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.firebaseNotification.FirebaseNotification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public class MarketActivity extends AppCompatActivity {

    private static final String WEBSOCKET_TAG = "websocket_tag";
    private static final String market_tag = "MARKET";
    private static final String exchange_tag = "EXCHANGE";
    private static final String prediction_tag = "PREDICTION";
    private static final String balance_tag = "BALANCE";

    // Initialize variables
    private DrawerLayout drawerLayout;
    private LinearLayout lyProfile, lyMarket, lyWithdraw, lyDeposit, lyDiscover, lyAboutUs, lyContactUs, lySupport, lyLogout;
    private RelativeLayout lyToolbar;
    private ImageView btnMenu, btnNotification;
    private BottomNavigationView navigationView;

    private Dialog progressLoading;

    private MarketViewModel marketViewModel;
    private final SharedMarketObject sharedMarketObject = new SharedMarketObject();
    private HashMap<RequestType, Request> listOfRequests = new HashMap<RequestType, Request>();

    private WebSocket webSocket;
    private final OkHttpClient client = new OkHttpClient();

    private final Gson gson = new Gson();
    private final Utils utils = new Utils();

    private final NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    // Fragments
    private final MarketFragmentK marketFragment = new MarketFragmentK();
    private final ExchangeFragment exchangeFragment = new ExchangeFragment();
    private final BalanceFragment balanceFragment = new BalanceFragment();
    private final PredictionFragment predictionFragment = new PredictionFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        // initialize view model and connect activity
        marketViewModel = new ViewModelProvider(this).get(MarketViewModel.class);

        // initiate view components
        initiateViewComponents();
        setDrawerMenuFunctionality();
        setupLoadWaitingBar();

        // drawer menu does not open by sliding right from edge
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //set connection timeout
        setConnectionTimeout();

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivityUtilities.ClickNotification(MarketActivity.this);
                Intent notificationCenterIntent = new Intent(MarketActivity.this, FirebaseNotification.class);
                startActivity(notificationCenterIntent);
            }
        });

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navExchange:
                    replaceFragment(MarketFragments.EXCHANGE);
                    break;
                case R.id.navPrediction:
                    replaceFragment(MarketFragments.PREDICTION);
                    break;
                case R.id.navBalance:
                    replaceFragment(MarketFragments.BALANCE);
                    break;
                default:
                    replaceFragment(MarketFragments.MARKET);
                    break;
            }
            return true;
        });

        // gets any extra data coming from outside of activity
        Bundle extraData = getIntent().getExtras();

        // if already any market selected user is directed to the exchange activity
        if (extraData != null) {
            sharedMarketObject.setMarket(gson.fromJson(extraData.getString("market"), MarketObject.class));
            navigationView.setSelectedItemId(R.id.navExchange);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, exchangeFragment, "exchange").commit();
        } else {
            // makes market fragment active
            navigationView.setSelectedItemId(R.id.navMarket);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, marketFragment, market_tag).commit();
        }

        listenViewModelChanges();
    }

    private void initiateViewComponents() {
        //Assign Variables
        drawerLayout = findViewById(R.id.drawerLayout);
        lyProfile = (LinearLayout) drawerLayout.findViewById(R.id.lyProfile);
        lyMarket = (LinearLayout) drawerLayout.findViewById(R.id.lyMarket);
        lyWithdraw = (LinearLayout) drawerLayout.findViewById(R.id.lyWithdraw);
        lyDeposit = (LinearLayout) drawerLayout.findViewById(R.id.lyDeposit);
        lyDiscover = (LinearLayout) drawerLayout.findViewById(R.id.lyDiscover);
        lyAboutUs = (LinearLayout) drawerLayout.findViewById(R.id.lyAboutUs);
        lyContactUs = (LinearLayout) drawerLayout.findViewById(R.id.lyContactUs);
        lySupport = (LinearLayout) drawerLayout.findViewById(R.id.lySupport);
        lyLogout = (LinearLayout) drawerLayout.findViewById(R.id.lyLogout);
        btnMenu = (ImageView) drawerLayout.findViewById(R.id.btnMenu);
        btnNotification = (ImageView) drawerLayout.findViewById(R.id.btnNotification);
        lyToolbar = (RelativeLayout) drawerLayout.findViewById(R.id.lyToolbar);
        navigationView = (BottomNavigationView) drawerLayout.findViewById(R.id.bottomNavBar);
    }

    private void setDrawerMenuFunctionality() {
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickMenuButton();
            }
        });
        lyMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickMarketItem(view);
            }
        });
        lyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickProfile(MarketActivity.this);
            }
        });
        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickWithdraw(MarketActivity.this);
            }
        });
        lyDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickDeposit(MarketActivity.this);
            }
        });
        lyDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickDiscover(MarketActivity.this);
            }
        });
        lyAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickAboutUs(MarketActivity.this);
            }
        });
        lyContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickContactUs(MarketActivity.this);
            }
        });
        lySupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickSupport(MarketActivity.this);
            }
        });
        lyLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickLogout(MarketActivity.this);
            }
        });
    }

    private void setupLoadWaitingBar() {
        progressLoading = new Dialog(this);
        utils.setupDialog(progressLoading, this);
    }

    private void replaceFragment(MarketFragments fragment) {
        switch (fragment){
            case EXCHANGE:
                unsubscribeFromSocket();
                Bundle bundle = new Bundle();
                bundle.putString("market", new GsonBuilder().create().toJson(sharedMarketObject.getMarket()));
                exchangeFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, exchangeFragment, exchange_tag).commit();
                break;
            case PREDICTION:
                unsubscribeFromSocket();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, predictionFragment, prediction_tag).commit();
                break;
            case BALANCE:
                unsubscribeFromSocket();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, balanceFragment, balance_tag).commit();
                break;
            default:
                unsubscribeFromSocket();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, marketFragment, market_tag).commit();
                break;
        }
    }

    public void ClickMenuButton() {
        //Open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout);
    }

    public void ClickMarketItem(View view) {
        // Recreate activity
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    private void setFragment(MarketFragments fragment){
        switch (fragment) {
            case MARKET:
                navigationView.setSelectedItemId(R.id.navMarket);
                break;
            case EXCHANGE:
                navigationView.setSelectedItemId(R.id.navExchange);
                break;
        }
        replaceFragment(fragment);
    }

    private void setConnectionTimeout() {
        client.newBuilder()
                .connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private void connectToSocket() {
        Request request = new Request.Builder().url("wss://wss.arsinex.com:8443/").tag(WEBSOCKET_TAG).build();
        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        webSocketDataObserver.setResponse(response);
                        marketViewModel.setSocketResponse(response);
                    }
                });
            }
        };
        webSocket = client.newWebSocket(request, webSocketListener);

        // unsubscribes from default updates coming from server
        sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString());
    }

    private void sendRequestToSocket(String request) {
        // show waiting bar
        marketViewModel.setShowWaitingBar(true);

        webSocket.send(request);
    }
    private void unsubscribeFromSocket() {
        MarketFragments activeFragment = getActiveFragment();
        if (activeFragment != null) {
            switch (activeFragment) {
                case MARKET:
                    sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString());
                    break;
                case EXCHANGE:
                    sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString());
                    sendRequestToSocket(getUnsubscribeRequest("deals.unsubscribe").toString());
                    sendRequestToSocket(getUnsubscribeRequest("depth.unsubscribe").toString());
                    break;
                case PREDICTION:
                    sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString());
                    sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString());
                    break;
                case BALANCE:
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Key, Prediction Card Type");
            }
        }
    }

    private JSONObject getUnsubscribeRequest(String method) {
        JSONArray paramsArray = new JSONArray();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("method", method)
                    .put("params", paramsArray)
                    .put("id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonRequest;
    }

    private MarketFragments getActiveFragment() {
        if (marketFragment != null && marketFragment.isVisible()) {
            return MarketFragments.MARKET;
        }
        if (exchangeFragment != null && exchangeFragment.isVisible()) {
            return MarketFragments.EXCHANGE;
        }
        if (predictionFragment != null && predictionFragment.isVisible()) {
            return MarketFragments.PREDICTION;
        }
        if (balanceFragment != null && balanceFragment.isVisible()) {
            return MarketFragments.BALANCE;
        }
        return null;
    }

    private void listenViewModelChanges() {

        // listens for status bar color change
        marketViewModel.getToolbarColor().observe(this, color -> {
            lyToolbar.setBackgroundColor(color);
        });

        marketViewModel.getMarket().observe(this, market -> {
            sharedMarketObject.setMarket(market);
        });

        marketViewModel.getFragment().observe(this, fragment -> {
            setFragment(fragment);
        });

        marketViewModel.getShowWaitingBar().observe(this, show -> {
            if (show && !progressLoading.isShowing()) {
                progressLoading.show();
            } else if (!show && progressLoading.isShowing()) {
                progressLoading.cancel();
            }
            displayShowCase();
        });

        // listens for any request to be send
        marketViewModel.getApiRequest().observe(this, requestPair -> {
            Request request = requestPair.first;
            RequestType requestType = requestPair.second;
            sendRequestToAPI(request, requestType);
        });

        marketViewModel.getSocketRequest().observe(this, request -> {
            webSocket.send(request);
        });
    }

    private void sendRequestToAPI(Request request, RequestType requestType) {
        // show waiting bar
        marketViewModel.setShowWaitingBar(true);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            marketViewModel.setRequestFailed(true, requestType);
                            call.cancel();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                int responseCode = response.code();
                String responseBody = response.body().string();
                response.close();
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (responseCode) {
                                case HttpURLConnection.HTTP_OK:
                                    marketViewModel.setApiResponse(responseBody, requestType);
                                    break;
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    progressLoading.cancel();
                                    try {
                                        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(requestType, responseBody);
                                        failureResponse(hashResponse);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_UNAUTHORIZED:
                                    progressLoading.cancel();
                                    unauthorizedResponse();
                                    break;
                                default:
                                    progressLoading.cancel();
                                    failureResponse(null);
                                    break;
                            }
                        }
                    });
                }
            }
        });

    }

    private void failureResponse(HashMap<Object, Object> hashResponse){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        String error_msg = (hashResponse != null) ? hashResponse.getOrDefault("error_msg", "").toString() : "";

        btnNegative.setVisibility(View.GONE); // No need for this button here!
        lblMsgHeader.setText(getResources().getString(R.string.server_msg));
        lblMsg.setText(error_msg);
        btnPositive.setText(getResources().getString(R.string.ok));

        dialog.setCancelable(true);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void unauthorizedResponse() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the custom layout
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        btnNegative.setVisibility(View.GONE); // No need for this button here!
        btnPositive.setText(getResources().getString(R.string.ok));

        lblMsgHeader.setText(getResources().getString(R.string.sessionTimeOut));
        lblMsg.setText(R.string.session_timeout_msg);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent loginActivity = new Intent(MarketActivity.this, LoginRegisterActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginActivity);
                finishAffinity();
            }
        });

        dialog.setCancelable(false);

        // show dialog
        dialog.show();
    }

    private void displayShowCase() {
        SharedPreferences preferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        if(!preferences.contains("showcase_visited"))
            if(getActiveFragment() == MarketFragments.MARKET) {
                new MarketShowcase(this, findViewById(android.R.id.content).getRootView()).startShowcase();
                SharedPreferences.Editor editor = getSharedPreferences("settings", Activity.MODE_PRIVATE).edit();
                editor.putBoolean("showcase_visited", true);
                editor.apply();
            }
    }

    private void askForLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View exitDialog = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(exitDialog);

        TextView lblMsgHeader = (TextView) exitDialog.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) exitDialog.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) exitDialog.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) exitDialog.findViewById(R.id.btnPositive);

        lblMsgHeader.setText(getResources().getString(R.string.exitQuestion));
        lblMsg.setVisibility(View.GONE);
        btnPositive.setText(getResources().getString(R.string.yes));
        btnNegative.setText(getResources().getString(R.string.no));
        AlertDialog dialog = builder.create();
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishAffinity();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private static void dismissAllDialogs(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();

        if (fragments == null)
            return;

        for (Fragment fragment : fragments) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismissAllowingStateLoss();
            }

            FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            if (childFragmentManager != null)
                dismissAllDialogs(childFragmentManager);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Close drawer
        unsubscribeFromSocket();
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    @Override
    protected void onDestroy() {
        // removes list of markets at the time of leaving market activity
        utils.removeFromSharedPreferences(this, "market_dict");
        webSocket.close(1000, null);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        dismissAllDialogs(this.getSupportFragmentManager());
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        MarketFragments activeFragment = getActiveFragment();
        if (activeFragment != null) {
            if (activeFragment == MarketFragments.MARKET) {
                askForLogout();
            } else {
                unsubscribeFromSocket();
                setFragment(MarketFragments.MARKET);
            }
        }
    }

}