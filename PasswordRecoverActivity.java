package com.arsinex.com;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arsinex.com.enums.RequestType;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.arsinex.com.Utilities.Utils;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PasswordRecoverActivity extends AppCompatActivity {

    private static final String TAG = "recover password";
    private static final String FORGET_PASSWORD_TAG = "forget_password";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int PHONE = 101;
    private static final int EMAIL = 102;

    private TextInputEditText txtEmail, txtPhone;
    private Button btnSend;
    private TextInputLayout lyTxtEmail, lyTxtPhone;
    private RadioButton rbEmailRecovery, rbPhoneRecovery;
    private TextView lblPassRecover;
    private LinearLayout lyPhoneSection;
    private ImageView btnBack;

    private final OkHttpClient client = new OkHttpClient();
    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_recover);

        lblPassRecover = (TextView) findViewById(R.id.lblPassRecover);
        txtEmail = (TextInputEditText) findViewById(R.id.txtEmail);
        txtPhone = (TextInputEditText) findViewById(R.id.txtPhone);
        rbEmailRecovery = (RadioButton) findViewById(R.id.rbEmailRecovery);
        rbPhoneRecovery = (RadioButton) findViewById(R.id.rbPhoneRecovery);
        btnSend = (Button) findViewById(R.id.btnSend);
        lyTxtEmail = (TextInputLayout) findViewById(R.id.lyTxtEmail);
        lyTxtPhone = (TextInputLayout) findViewById(R.id.lyTxtPhone);
        lyPhoneSection = (LinearLayout) findViewById(R.id.lyPhoneSection);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        // set up active buttons
        rbEmailRecovery.setChecked(true);
        rbPhoneRecovery.setChecked(false);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lyTxtEmail.setErrorEnabled(false);

            }
        });

        txtPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lyTxtPhone.setErrorEnabled(false);
            }
        });

        rbEmailRecovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if (check) {
                    lblPassRecover.setText(R.string.recoverPasswordByEmail);
                    rbEmailRecovery.setChecked(true);
                    lyTxtEmail.setVisibility(View.VISIBLE);
                    rbPhoneRecovery.setChecked(false);
                    lyPhoneSection.setVisibility(View.GONE);

                }
            }
        });

        rbPhoneRecovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                 if (check){
                     lblPassRecover.setText(R.string.recoverPasswordByPhone);
                     rbPhoneRecovery.setChecked(true);
                     lyPhoneSection.setVisibility(View.VISIBLE);
                     rbEmailRecovery.setChecked(false);
                     lyTxtEmail.setVisibility(View.GONE);
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbEmailRecovery.isChecked()) {
                    requestWithEmail();
                } else {
                    requestWithPhone();
                }
            }
        });
    }

    private void requestWithEmail() {
        String email = txtEmail.getText().toString();
        if (utils.isEmailValid(email)) {
            btnSend.setClickable(false);
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("email", txtEmail.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody = RequestBody.create(jsonRequest.toString(), JSON);
            final String url = new RequestUrls().getUrl(RequestType.FORGET_PASSWORD_EMAIL);
            sendRequest(EMAIL, new Request.Builder().url(url).post(requestBody).tag(FORGET_PASSWORD_TAG).build());
        } else {
            lyTxtEmail.setErrorEnabled(true);
            lyTxtEmail.setError(getResources().getString(R.string.not_valid_email));
        }
    }

    private void requestWithPhone() {
        String phone = txtPhone.getText().toString();
        if (phone.length() > 4) {
            btnSend.setClickable(false); // TODO activate it after doing the following part
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("phone", txtPhone.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody = RequestBody.create(jsonRequest.toString(), JSON);
            final String url = new RequestUrls().getUrl(RequestType.FORGET_PASSWORD_SMS);
            sendRequest(PHONE, new Request.Builder().url(url).post(requestBody).tag(FORGET_PASSWORD_TAG).build());
        } else {
            lyTxtPhone.setErrorEnabled(true);
            lyTxtPhone.setError(getResources().getString(R.string.not_valid_phone));
        }
    }


    private void sendRequest(int type, Request request) {
        client.newBuilder().connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                btnSend.setClickable(true);
                Log.d(TAG,e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSend.setClickable(true);
                        try {
                            handleResponse(type, response);
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void handleResponse(int type, Response response) throws IOException, JSONException {
        if (response.code() == HttpsURLConnection.HTTP_OK) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (jsonResponse.has("success")) {
                showResponseDialog(type, true, null);
            } else {
                showResponseDialog(type, false, null);
            }
        } else {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            showResponseDialog(type, false, jsonResponse.getString("message"));
        }
    }


    private void showResponseDialog(int type, boolean success, @Nullable String server_response){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);

        btnNegative.setVisibility(View.GONE); // No need for this button here!

        lblMsgHeader.setText(getResources().getString(R.string.recover_password_header));

        if(success) {
            if(type == EMAIL) {
                lblMsg.setText(getResources().getString(R.string.forget_password_email_send));
            } else {
                lblMsg.setText(getResources().getString(R.string.forget_password_msg_send));
            }
        } else if (!server_response.equals(null)) {
            lblMsg.setText(server_response);
        } else {
            lblMsg.setText(getResources().getString(R.string.error));
        }

        btnPositive.setText(getResources().getString(R.string.done));
        dialog.setCancelable(true);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
