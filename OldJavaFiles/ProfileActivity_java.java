package com.arsinex.com.OldJavaFiles;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.arsinex.com.APIResponseParser;
import com.arsinex.com.KYCVerification.KycVerificationStep1Activity;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.PasswordRecoverActivity;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.MainActivityUtilities;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity_java extends AppCompatActivity {

    private final static String USER_TAG = "user";

    public static final String WAITING = "0";
    public static final String CONFIRMED = "1";
    public static final String NOT_CONFIRMED = "2";

    private DrawerLayout drawerLayout;
    private CardView lyGoToKYCActivation;
    private LinearLayout lyProfile, lyMarket, lyAboutUs, lyWithdraw, lyDeposit, lyDiscover, lyLogout, lyContactUs, lySupport, lyMainSection;
    private RelativeLayout lyChangePassword;
    private TextView lblEmail, lblPhone, lblFullName, lblKYCStatus;

    private final OkHttpClient client = new OkHttpClient();

    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private final Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //Assign Variables
        lyProfile = (LinearLayout) drawerLayout.findViewById(R.id.lyProfile);
        lyMarket = (LinearLayout) drawerLayout.findViewById(R.id.lyMarket);
        lyAboutUs = (LinearLayout) drawerLayout.findViewById(R.id.lyAboutUs);
        lyWithdraw = (LinearLayout) drawerLayout.findViewById(R.id.lyWithdraw);
        lyDeposit = (LinearLayout) drawerLayout.findViewById(R.id.lyDeposit);
        lyDiscover = (LinearLayout) drawerLayout.findViewById(R.id.lyDiscover);
        lyContactUs = (LinearLayout) drawerLayout.findViewById(R.id.lyContactUs);
        lySupport = (LinearLayout) drawerLayout.findViewById(R.id.lySupport);
        lyLogout = (LinearLayout) drawerLayout.findViewById(R.id.lyLogout);

        lyMainSection = (LinearLayout) findViewById(R.id.lyMainSection);
        lyGoToKYCActivation = (CardView) findViewById(R.id.lyGoToKYCActivation);
        lyChangePassword = (RelativeLayout) findViewById(R.id.lyChangePassword);
        lblEmail = (TextView) findViewById(R.id.lblEmail);
        lblPhone = (TextView) findViewById(R.id.lblPhone);
        lblFullName = (TextView) findViewById(R.id.lblFullName);
        lblKYCStatus = (TextView) findViewById(R.id.lblKYCStatus);

        getUserInfo();

        lyMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickMarket(ProfileActivity_java.this);
            }
        });

        lyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickProfile();
            }
        });

        lyAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickAboutUs(ProfileActivity_java.this);
            }
        });

        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickWithdraw(ProfileActivity_java.this);
            }
        });

        lyDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickDeposit(ProfileActivity_java.this);
            }
        });

        lyDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickDiscover(ProfileActivity_java.this);
            }
        });

        lyContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickContactUs(ProfileActivity_java.this);
            }
        });

        lySupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickSupport(ProfileActivity_java.this);
            }
        });

        lyLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickLogout(ProfileActivity_java.this);
            }
        });

        lyGoToKYCActivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent kycVerificationStep1Activity = new Intent(ProfileActivity_java.this, KycVerificationStep1Activity.class);
                startActivity(kycVerificationStep1Activity);
            }
        });

        lyChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recoverPasswordActivity = new Intent(ProfileActivity_java.this, PasswordRecoverActivity.class);
                startActivity(recoverPasswordActivity);
            }
        });
    }

    private void getUserInfo() {
        String url = new RequestUrls().getUrl(RequestType.USER);
        RequestBody requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(USER_TAG).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                    try {
                                        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(RequestType.USER, responseBody);
                                        updateProfileView(hashResponse);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    try {
                                        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(RequestType.USER, response.body().string());
                                        failureResponse(hashResponse);
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_UNAUTHORIZED:
                                    unauthorizedResponse();
                                    break;
                                default:
                                    failureResponse(null);
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateProfileView(HashMap<Object, Object> hashResponse){
        String email = hashResponse.get("email").toString();
        String phone = hashResponse.get("phone").toString();
        if (email.equals("null")) {
            lblEmail.setHint(getResources().getString(R.string.not_registered));
        } else {
            lblEmail.setText(email);
        }
        if (phone.equals("null")) {
            lblPhone.setHint(getResources().getString(R.string.not_registered));
        } else {
            lblPhone.setText(phone);
        }
        lblFullName.setText(hashResponse.get("name") + " " + hashResponse.get("surname"));
        if (hashResponse.get("kyc_confirm").equals(NOT_CONFIRMED)) {
            lyGoToKYCActivation.setVisibility(View.VISIBLE);
            lblKYCStatus.setVisibility(View.VISIBLE);
            lblKYCStatus.setText(R.string.activation_needed);
            lblKYCStatus.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        } else if (hashResponse.get("kyc_confirm").equals(CONFIRMED)) {
            lyGoToKYCActivation.setVisibility(View.GONE);
            lblKYCStatus.setVisibility(View.VISIBLE);
            lblKYCStatus.setText(R.string.verified);
            lblKYCStatus.setTextColor(ContextCompat.getColor(this, R.color.green_1));
        } else if (hashResponse.get("kyc_confirm").equals(WAITING)){
            lyGoToKYCActivation.setVisibility(View.GONE);
            lblKYCStatus.setVisibility(View.VISIBLE);
            lblKYCStatus.setText(R.string.under_review);
            lblKYCStatus.setTextColor(ContextCompat.getColor(this, R.color.mainGold));
        }
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
                Intent loginActivity = new Intent(ProfileActivity_java.this, LoginRegisterActivity.class);
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

    public void ClickMenu(View view) {
        //Open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout);
    }

    public void ClickProfile() {
        // Recreate activity
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}