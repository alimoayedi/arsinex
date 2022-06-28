package com.arsinex.com.NotificationCenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationUtility {

    private static final String GET_KYC_TAG = "get_kyc";

    private Activity activity;

    private final OkHttpClient client = new OkHttpClient();
    private final Utils utils = new Utils();

    public NotificationUtility(Activity activity){
        this.activity = activity;
    }

    public void getKYCStatus() {
        final String requestURL = new RequestUrls().getUrl(RequestType.USER);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(activity, "token")).post(requestBody).tag(GET_KYC_TAG).build();
        makeAPIRequest(request);
    }

    private void makeAPIRequest(Request request) {
        setConnectionTimeout();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            call.cancel();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response.code()) {
                                case HttpURLConnection.HTTP_OK:
                                    try {
                                        parseResponse(response.body().string());
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_UNAUTHORIZED:
                                    unauthorizedResponse();
                                    break;
                                default:
                                    break;
                            }
                            response.close();
                        }
                    });
                }
            }
        });
    }

    private void setConnectionTimeout() {
        client.newBuilder().connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);
    }

    private void parseResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        NotificationCenterActivity.notificationDataObserver.setResponse(jsonResponse.getString("kyc_confirm"));
    }

    private void unauthorizedResponse() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getBaseContext());

        // set the custom layout
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        btnNegative.setVisibility(View.GONE); // No need for this button here!
        btnPositive.setText(activity.getResources().getString(R.string.ok));

        lblMsgHeader.setText(activity.getResources().getString(R.string.sessionTimeOut));
        lblMsg.setText(R.string.session_timeout_msg);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent loginActivity = new Intent(activity, LoginRegisterActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(loginActivity);
                activity.finishAffinity();
            }
        });
        dialog.setCancelable(false);

        // show dialog
        dialog.show();
    }

}
