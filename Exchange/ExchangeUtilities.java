package com.arsinex.com.Exchange;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.arsinex.com.Objects.MarketObject;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.MarketAction;
import com.arsinex.com.enums.OrderType;
import com.arsinex.com.enums.Percentage;
import com.arsinex.com.enums.RequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ExchangeUtilities {

    private static final String TAG = "**************** EXCHANGE UTILITIES *****************";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String BALANCE_TAG = "balance";
    private static final String PUT_LIMIT_ORDER_TAG = "put_limit";
    private static final String PUT_MARKET_ORDER_TAG = "put_limit";

    private Utils utils = new Utils();
    private final OkHttpClient client = new OkHttpClient();


    private Activity activity;
    private MarketObject marketObject;

    public ExchangeUtilities(Activity activity, MarketObject marketObject) {
        this.activity = activity;
        this.marketObject = marketObject;
    }

    /**
     *
     * @param orderType
     * @param lblBalance
     * @param txtPrice
     * @param txtQuantity
     * @param txtTotal
     * @return
     */
    public boolean buyErrorCheck(OrderType orderType, String lblBalance, String txtPrice, String txtQuantity, String txtTotal) {
        if(lblBalance.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.balance_not_fetched), Toast.LENGTH_SHORT).show();
            return false;
        }

        // this field is only check for LIMIT type of order
        if(orderType == OrderType.LIMIT && txtPrice.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.price_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(txtQuantity.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.quantity_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        // this field is only check for LIMIT type of order
        if(orderType == OrderType.LIMIT && !txtPrice.matches("\\d+(?:\\.\\d+)?")) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.incorrect_price), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!txtQuantity.matches("\\d+(?:\\.\\d+)?")) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.incorrect_quantity), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!txtTotal.matches("\\d+(?:\\.\\d+)?")) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.incorrect_total), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Double.valueOf(txtTotal) > Double.valueOf(lblBalance)) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.low_balance), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     *
     * @param orderType
     * @param lblBalance
     * @param txtPrice
     * @param txtQuantity
     * @return
     */
    public boolean sellErrorCheck(OrderType orderType, String lblBalance, String txtPrice, String txtQuantity) {
        if (lblBalance.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.balance_not_fetched), Toast.LENGTH_SHORT).show();
            return false;
        }

        // this field is only check for LIMIT type of order
        if (orderType == OrderType.LIMIT && txtPrice.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.price_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(txtQuantity.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.quantity_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        // this field is only check for LIMIT type of order
        if(orderType == OrderType.LIMIT && !txtPrice.matches("\\d+(?:\\.\\d+)?")) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.incorrect_price), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!txtQuantity.matches("\\d+(?:\\.\\d+)?")) {
            Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.incorrect_quantity), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Double.valueOf(txtQuantity.toString()) > Double.valueOf(lblBalance.toString())) {
            Toast.makeText(activity, activity.getResources().getString(R.string.low_balance), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public String calculateFraction(MarketAction action, String balance, String last_price, Percentage share) {
        String price = utils.standardizeTurkishStrings(utils.reduceDecimal(last_price,8));
        switch (share) {
            case QUARTER:
                if (action == MarketAction.BUY) {
                    return utils.reduceDecimal(String.valueOf(0.25 * Double.parseDouble(balance) / Double.parseDouble(price)),8);
                } else if (action == MarketAction.SELL) {
                    return utils.reduceDecimal(String.valueOf(0.25 * Double.parseDouble(balance)),8);
                }
                break;
            case HALF:
                if (action == MarketAction.BUY) {
                    return utils.reduceDecimal(String.valueOf(0.5 * Double.parseDouble(balance) / Double.parseDouble(price)),8);
                } else if (action == MarketAction.SELL) {
                    return utils.reduceDecimal(String.valueOf(0.5 * Double.parseDouble(balance)),8);
                }
                break;
            case TRI_QUARTER:
                if (action == MarketAction.BUY) {
                    return utils.reduceDecimal(String.valueOf(0.75 * Double.parseDouble(balance) / Double.parseDouble(price)),8);
                } else if (action == MarketAction.SELL) {
                    return utils.reduceDecimal(String.valueOf(0.75 * Double.parseDouble(balance)),8);
                }
                break;
            case FULL:
                if (action == MarketAction.BUY) {
                    return utils.reduceDecimal(String.valueOf(Double.parseDouble(balance) / Double.parseDouble(price)),8);
                } else if (action == MarketAction.SELL) {
                    return utils.reduceDecimal(balance,8);
                }
                break;
        }
        return "";
    }

    public void calculateTotal(String price, String amount, EditText txtTotal) {
        DecimalFormat decimalFormatter = new DecimalFormat("#.############");
        decimalFormatter.setMinimumFractionDigits(2);
        decimalFormatter.setMaximumFractionDigits(8);
        // checks price and amount should not be empty and checks if amount is a number
        if(!price.isEmpty() && !amount.isEmpty() && amount.matches("\\d+(?:\\.\\d+)?")){
            txtTotal.setText(decimalFormatter.format(Double.parseDouble(price) * Double.parseDouble(amount)));
        }
    }

    public void calculateQuantity(String price, String total, EditText txtQuantity) {
        DecimalFormat decimalFormatter = new DecimalFormat("#.############");
        decimalFormatter.setMinimumFractionDigits(2);
        decimalFormatter.setMaximumFractionDigits(8);
        if (!price.isEmpty() && !total.isEmpty() && total.matches("\\d+(?:\\.\\d+)?")) {
            txtQuantity.setText(decimalFormatter.format(Double.parseDouble(total) / Double.parseDouble(price)));
        }
    }

//    public void fetchBalance(View view, int action) {
//        String requestURL = new RequestsClass().getURL(RequestsClass.USER_BALANCE);
//        RequestBody requestBody = RequestBody.create(new byte[0]);
//        Request request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(activity, "token")).post(requestBody).tag(BALANCE_TAG).build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        call.cancel();
//                        failureRequest(view, action);
//                    }
//                });
//            }
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (activity != null && !activity.isFinishing() && !activity.getFragmentManager().getFragments().get(0).isRemoving()) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (response.code() == HttpURLConnection.HTTP_OK) {
//                                try {
//                                    String stringResponse = response.body().string();
//                                    HashMap<String, String> balance = parseBalanceResponse(stringResponse);
//                                    updateBalance(view, action, balance);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                failureResponse(response.code());
//                            }
//                            response.close();
//                        }
//                    });
//                }
//            }
//        });
//    }


    public Request generateLimitRequest(MarketAction action, String market, String amount, String price) {
        final String url = new RequestUrls().getUrl(RequestType.PUT_LIMIT_ORDER);

        JSONObject jsonObjectRequest = null;

        try {
            jsonObjectRequest = new JSONObject()
                    .put("type", (action == MarketAction.BUY) ? 2 : 1)
                    .put("amount", Double.parseDouble(amount))
                    .put("price", Double.parseDouble(price))
                    .put("market", market);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
        return new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(activity, "token")).post(requestBody).tag(PUT_LIMIT_ORDER_TAG).build();
    }
    public Request generateMarketRequest(MarketAction action, String market, String amount) {
        final String url = new RequestUrls().getUrl(RequestType.PUT_MARKET_ORDER);

        JSONObject jsonObjectRequest = null;

        try {
            jsonObjectRequest = new JSONObject()
                    .put("type", (action == MarketAction.BUY) ? 2 : 1)
                    .put("amount", Double.parseDouble(amount))
                    .put("market", market);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonObjectRequest.toString(), JSON);
        return new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(activity, "token")).post(requestBody).tag(PUT_MARKET_ORDER_TAG).build();
    }

    public HashMap<String, String> parseBalanceResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            HashMap<String, String> balance = new HashMap<>();
            balance.put(marketObject.getStock(), jsonResponse.getJSONObject("result").getJSONObject(marketObject.getStock()).getString("available"));
            balance.put(marketObject.getMoney(), jsonResponse.getJSONObject("result").getJSONObject(marketObject.getMoney()).getString("available"));
            return balance;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    public void parseBuySellOrder(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        if(jsonObject.has("error") && jsonObject.getBoolean("error")) {
            orderPlacedAlert(false, jsonObject.getString("message"));
        } else {
            orderPlacedAlert(true, jsonObject.getString("message"));
        }
    }

    private void orderPlacedAlert(boolean result, String msg){
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.findViewById(android.R.id.content).getContext());

        // set the custom layout
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
        btnNegative.setVisibility(View.GONE); // No need for this button here!
        lblMsgHeader.setText(activity.getResources().getString(R.string.placing_order));
        btnPositive.setText(activity.getString(R.string.ok));

        if (result) {
            lblMsg.setText(activity.getResources().getString(R.string.order_successful) + "\n" + activity.getResources().getString(R.string.server_msg) + msg);
        } else {
            lblMsg.setText(activity.getResources().getString(R.string.order_failed) + "\n" + activity.getResources().getString(R.string.server_msg) + msg);
        }

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // show dialog
        dialog.show();
    }

}
