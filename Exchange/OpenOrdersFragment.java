package com.arsinex.com.Exchange;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.marketPackage.MarketViewModel;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.Objects.OpenOrderObject;
import com.arsinex.com.Objects.SharedMarketObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OpenOrdersFragment extends Fragment {

    private static final String TAG = "***************** OPEN ORDER *****************";
    private static final String OPEN_ORDERS_TAG = "openOrders";
    private static final String REMOVE_ORDER_TAG = "removeOrder";

    private static final int OPEN_ORDER_LIMIT = 100;
    private static final int OPEN_ORDER_OFFSET = 0;

    private Utils utils = new Utils();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ListView listOpenOrders;
    private TextView lblNoRecord;
    private ProgressBar progressLoadingOpenOrders;
    private ArrayList<OpenOrderObject> openOrdersList = new ArrayList<>();
    private OpenOrdersAdaptor openOrdersAdaptor;

    private SharedMarketObject sharedMarketObject = new SharedMarketObject();

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View openOrdersView = inflater.inflate(R.layout.layout_open_orders, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        // gets market from market activity
        sharedMarketObject.setMarket(new GsonBuilder().create().fromJson(getArguments().getString("market"), MarketObject.class));

        listOpenOrders = (ListView) openOrdersView.findViewById(R.id.listOpenOrders);
        lblNoRecord = (TextView) openOrdersView.findViewById(R.id.lblNoRecord);
        progressLoadingOpenOrders = (ProgressBar) openOrdersView.findViewById(R.id.progressLoadingOpenOrders);

        // sets adaptor
        openOrdersAdaptor = new OpenOrdersAdaptor(this.getContext(), openOrdersList);

        uiToLoadingMode();

        setAPIRequest(RequestType.ORDER_PENDING, null);

        listOpenOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView lblAction = (TextView) view.findViewById(R.id.lblAction);
                lblAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmOrderCancellation(position);
                    }
                });
            }
        });

        listenForComingDataFromServer();

        return openOrdersView;
    }

    private void uiToLoadingMode() {
        progressLoadingOpenOrders.setVisibility(View.VISIBLE);
        listOpenOrders.setVisibility(View.GONE);
        lblNoRecord.setVisibility(View.GONE);
    }

    private void setAPIRequest(RequestType requestType, String dataToPass){
        RequestBody requestBody;
        Request request = null;
        JSONObject jsonObjectRequest = new JSONObject();
        final String url = new RequestUrls().getUrl(requestType);

        switch (requestType) {
            case ORDER_PENDING:
                try {
                    jsonObjectRequest
                            .put("market", sharedMarketObject.getMarket().getName())
                            .put("limit", OPEN_ORDER_LIMIT)
                            .put("offset", OPEN_ORDER_OFFSET);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
                request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).tag(OPEN_ORDERS_TAG).post(requestBody).build();
                break;
            case ORDER_CANCEL:
                try {
                    jsonObjectRequest
                            .put("market", sharedMarketObject.getMarket().getName())
                            .put("order_id", Integer.valueOf(dataToPass));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
                request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).tag(REMOVE_ORDER_TAG).post(requestBody).build();
                break;
        }

        marketViewModel.setApiRequest(request, requestType);
    }

    private void listenForComingDataFromServer() {
        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            switch (requestType) {
                case ORDER_PENDING:
                    try {
                        ParseOpenOrders(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ORDER_CANCEL:
                    try {
                        ParseCancelOrderResponse(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            RequestType requestType = failurePair.second;

            if (requestType == RequestType.ORDER_PENDING) {
                setAPIRequest(RequestType.ORDER_PENDING, null);
            }
        });
    }

    private void ParseOpenOrders(String response) throws JSONException {
        progressLoadingOpenOrders.setVisibility(View.GONE);
        openOrdersList.clear();
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("error")) {
            listOpenOrders.setVisibility(View.GONE);
            lblNoRecord.setText(R.string.not_authorized);
            lblNoRecord.setVisibility(View.VISIBLE);
            return;
        }
        JSONArray ordersArray = jsonResponse.getJSONArray("records");
        for(int INDEX = 0; INDEX < ordersArray.length(); INDEX++) {
            JSONObject order = ordersArray.getJSONObject(INDEX);
            openOrdersList.add(new OpenOrderObject(
                    order.getString("side"),
                    order.getString("id"),
                    order.getString("market"),
                    order.getString("amount"),
                    order.getString("price"),
                    order.getString("maker_fee"),
                    order.getString("taker_fee"),
                    order.getString("type"),
                    sharedMarketObject.getMarket().getMoney_prec(),
                    sharedMarketObject.getMarket().getStock_prec(),
                    order.getDouble("ctime")

            ));

        }
        if (openOrdersList.size() == 0) {
            listOpenOrders.setVisibility(View.GONE);
            lblNoRecord.setVisibility(View.VISIBLE);
        } else {
            updateOrdersList();
        }
    }
    private void updateOrdersList() {
        listOpenOrders.setVisibility(View.VISIBLE);
        if (listOpenOrders.getChildCount() == 0) {
            listOpenOrders.setAdapter(openOrdersAdaptor);
        } else {
            openOrdersAdaptor.notifyDataSetChanged();
        }
    }
    private void ParseCancelOrderResponse(String response) throws JSONException {
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getString("status").equals("true")) {
                setAPIRequest(RequestType.ORDER_PENDING, null);
                showSnackBar();
            } else {
                showErrorMsg();
            }
    }
    private void showSnackBar() {
        // refresh open orders list
        final Snackbar snackbar = Snackbar.make(getView(), "", Snackbar.LENGTH_LONG);

        // inflate the custom_snackbar_view created previously
        View customSnackView = getLayoutInflater().inflate(R.layout.layout_snackbar, null);

        // set the background of the default snackbar as transparent
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        // now change the layout of the snackbar
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

        // set padding of the all corners as 0
        snackbarLayout.setPadding(0, 0, 0, 0);

        // register the button from the custom_snackbar_view layout file
        TextView lblMessage = customSnackView.findViewById(R.id.lblMessage);
        Button btnDone = customSnackView.findViewById(R.id.btnDone);

        lblMessage.setText(getResources().getString(R.string.order_cancelled));
        btnDone.setText(getResources().getString(R.string.done));

        // now handle the same button with onClickListener
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        // add the custom snack bar layout to snackbar layout
        snackbarLayout.addView(customSnackView, 0);

        snackbar.show();
    }
    private void showErrorMsg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // builder.setTitle("Cancel Order Confirmation");

        // set the custom layout
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
        btnNegative.setVisibility(View.GONE); // No need for this button here!
        lblMsgHeader.setText(getResources().getString(R.string.default_error_msg));
        lblMsg.setText(getResources().getString(R.string.unsuccessful_order_remove));
        btnPositive.setText(getResources().getString(R.string.ok));

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void confirmOrderCancellation(int itemPosition) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        lblMsgHeader.setText(getResources().getString(R.string.remove_order_header));
        lblMsg.setText(getResources().getString(R.string.remove_order_msg));
        btnNegative.setText(getResources().getString(R.string.cancel));
        btnPositive.setText(getResources().getString(R.string.remove));

        AlertDialog dialog = builder.create(); // create alert dialog

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAPIRequest(RequestType.ORDER_CANCEL, openOrdersList.get(itemPosition).getId());
                dialog.dismiss();
            }
        });

        // show dialog
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}