package com.arsinex.com.LoginRegister;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arsinex.com.APIResponseParser;
import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.KYCVerification.KycVerificationStep1Activity;
import com.arsinex.com.marketPackage.MarketActivity;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.LoginRegistrationFragments;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.marketPackage.MarketActivityK;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginRegisterActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String USER_TAG = "user";
    private static final String LOGIN_TAG = "login";
    private static final String REGISTER_TAG = "register";
    private static final String VERIFICATION_TAG = "verification";
    private static final String CODE_CONFIRM_TAG = "code_confirm";
    private static final String ADD_EMAIL_TAG = "add_email";
    private static final String ADD_PHONE_TAG = "add_phone";

    private static final int WAITING = 0;
    private static final int CONFIRMED = 1;
    private static final int NOT_CONFIRMED = 2;

    private HashMap<RequestType, JSONObject> savedRequests = new HashMap<RequestType, JSONObject>();

    private final OkHttpClient client = new OkHttpClient();

    private LoginRegisterViewModel loginRegisterViewModel;

    private final LoginFragment loginFragment = new LoginFragment();
    private final RegistrationFragment registrationFragment = new RegistrationFragment();
    private VerificationFragment verificationFragment;
    private final AddEmailFragment addEmailFragment = new AddEmailFragment();
    private final AddPhoneFragment addPhoneFragment = new AddPhoneFragment();

    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private Utils utils = new Utils();

    private Dialog progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_registration);

        loginRegisterViewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        setupLoadWaitingBar();

        setFragment(LoginRegistrationFragments.LOGIN, null);

        setConnectionTimeout();
        listenViewModelChanges();

    }

    private void setupLoadWaitingBar() {
        progressLoading = new Dialog(this);
        utils.setupDialog(progressLoading, this);
    }

    private void setFragment(LoginRegistrationFragments fragment, Pair<RequestType, String> data){
        // change fragment
        switch (fragment) {
            case LOGIN:
                getSupportFragmentManager().beginTransaction().replace(R.id.lyFragmentFrame, loginFragment, LOGIN_TAG).commit();
                break;
            case REGISTER:
                getSupportFragmentManager().beginTransaction().replace(R.id.lyFragmentFrame, registrationFragment, REGISTER_TAG).commit();
                break;
            case VERIFY:
                RequestType requestType = data.first;
                String contactInfo = data.second;
                verificationFragment = VerificationFragment.newInstance(requestType, contactInfo);
                getSupportFragmentManager().beginTransaction().replace(R.id.lyFragmentFrame, verificationFragment, VERIFICATION_TAG).commit();
                break;
            case ADD_EMAIL:
                getSupportFragmentManager().beginTransaction().replace(R.id.lyFragmentFrame, addEmailFragment, ADD_EMAIL_TAG).commit();
                break;
            case ADD_PHONE:
                getSupportFragmentManager().beginTransaction().replace(R.id.lyFragmentFrame, addPhoneFragment, ADD_PHONE_TAG).commit();
                break;
            default:
                throw new IllegalStateException("Unexpected value: Registration Activity, Set Fragment");
        }
    }

    private void listenViewModelChanges() {

        loginRegisterViewModel.getShowWaitingBar().observe(this, show -> {
            if (show && !progressLoading.isShowing()) {
                progressLoading.show();
            } else {
                progressLoading.cancel();
            }
        });

        loginRegisterViewModel.getFragment().observe(this, fragment -> {
            setFragment(fragment, null);
        });

        loginRegisterViewModel.getJsonRequest().observe(this, requestPair -> {
            Request request = null;

            JSONObject jsonRequest = requestPair.first;
            RequestType requestType = requestPair.second;

            final String requestURL = new RequestUrls().getUrl(requestType);
            RequestBody requestBody = RequestBody.create(new byte[0]);

            if (jsonRequest != null) {
                requestBody = RequestBody.create(jsonRequest.toString(), JSON);
            }

            switch (requestType) {
                case LOGIN:
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(LOGIN_TAG).build();
                    break;
                case USER:
                    request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(USER_TAG).build();
                    break;
                case REGISTER_EMAIL:
                case REGISTER_PHONE:
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(REGISTER_TAG).build();
                    saveRequest(jsonRequest, requestType);
                    break;
                case ADD_EMAIL:
                    request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(ADD_EMAIL_TAG).build();
                    saveRequest(jsonRequest, requestType);
                    break;
                case ADD_PHONE:
                    request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(ADD_PHONE_TAG).build();
                    saveRequest(jsonRequest, requestType);
                    break;
                case CODE_CONFIRMATION_EMAIL:
                case CODE_CONFIRMATION_PHONE:
                case ADD_EMAIL_CONFIRM:
                case ADD_PHONE_CONFIRM:
                    request = new Request.Builder().url(requestURL).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(CODE_CONFIRM_TAG).build();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: Registration Activity, Listen ViewModel");
            }

            sendRequestToAPI(request, requestType);
        });

        loginRegisterViewModel.getResendRequest().observe(this, requestType -> {
            Request request = null;
            RequestBody requestBody = null;

            // url of register requests and resends are similar
            final String requestURL = new RequestUrls().getUrl(requestType);

            switch (requestType) {
                case RESEND_REGISTER_EMAIL:
                    requestBody = RequestBody.create(savedRequests.get(RequestType.REGISTER_EMAIL).toString(), JSON);
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(REGISTER_TAG).build();
                    break;
                case RESEND_REGISTER_PHONE:
                    requestBody = RequestBody.create(savedRequests.get(RequestType.REGISTER_PHONE).toString(), JSON);
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(REGISTER_TAG).build();
                    break;
                case RESEND_ADD_EMAIL:
                    requestBody = RequestBody.create(savedRequests.get(RequestType.ADD_EMAIL).toString(), JSON);
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(REGISTER_TAG).build();
                    break;
                case RESEND_ADD_PHONE:
                    requestBody = RequestBody.create(savedRequests.get(RequestType.ADD_PHONE).toString(), JSON);
                    request = new Request.Builder().url(requestURL).post(requestBody).tag(REGISTER_TAG).build();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: Registration Activity, Listen ViewModel");
            }

            sendRequestToAPI(request, requestType);
        });

        loginRegisterViewModel.getCancelPendingRequest().observe(this, requestType -> {
            if(savedRequests.containsKey(requestType)) {
                savedRequests.remove(requestType);
            }
        });
    }

    private void setConnectionTimeout() {
        client.newBuilder()
                .connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private void saveRequest(JSONObject jsonRequest, RequestType requestType){
        savedRequests.put(requestType, jsonRequest);
    }

    private void sendRequestToAPI(Request request, RequestType requestType) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            failureRequest(requestType); // TODO
                            call.cancel();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                String responseString = response.body().string();
                int responseCode = response.code();
                response.close();

                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (responseCode) {
                                case HttpURLConnection.HTTP_OK:
                                    try {
                                        succeededResponse(requestType, responseString);
                                    } catch (JSONException e) {
                                        HashMap<Object, Object> hashResponse = (HashMap<Object, Object>) new HashMap<Object, Object>().put("error_msg", R.string.server_not_responding);
                                        loginRegisterViewModel.setRequestFailed(true);
                                        failureResponse(hashResponse);
                                        progressLoading.cancel();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    try {
                                        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(requestType, responseString);
                                        loginRegisterViewModel.setRequestFailed(true);
                                        failureResponse(hashResponse);
                                        progressLoading.cancel();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HttpURLConnection.HTTP_UNAUTHORIZED:
                                    loginRegisterViewModel.setRequestFailed(true);
                                    unauthorizedResponse(requestType);
                                    progressLoading.cancel();
                                    break;
                                default:
                                    loginRegisterViewModel.setRequestFailed(true);
                                    failureResponse(null);
                                    progressLoading.cancel();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void succeededResponse(RequestType requestType, String response) throws JSONException {
        HashMap<Object, Object> hashResponse = new APIResponseParser().parseResponse(requestType, response);
        if(hashResponse.containsKey("hasError") && (Boolean) hashResponse.get("hasError")) {
            failureResponse(hashResponse);
        } else {
            handleSuccess(requestType, hashResponse);
        }
        progressLoading.cancel();
    }

    private void handleSuccess(RequestType requestType, HashMap<Object, Object> hashResponse) throws JSONException {
        Intent nextIntent = null;
        switch (requestType) {
            case LOGIN:
                //save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // sends a new request to check user's condition
                loginRegisterViewModel.setJsonRequest(null, RequestType.USER);
                break;
            case USER:
                if ((int) hashResponse.get("phone_confirm") == NOT_CONFIRMED || hashResponse.get("phone_confirm").equals("null")) {
                    setFragment(LoginRegistrationFragments.ADD_PHONE, null);
                } else if ((int) hashResponse.get("mail_confirm") == NOT_CONFIRMED || hashResponse.get("mail_confirm").equals("null")) {
                    setFragment(LoginRegistrationFragments.ADD_EMAIL, null);
                } else if ((int) hashResponse.get("kyc_confirm") == NOT_CONFIRMED || hashResponse.get("kyc_confirm").equals("null")) {
                    Intent kycIntent = new Intent(LoginRegisterActivity.this, KycVerificationStep1Activity.class);
                    startActivity(kycIntent);
                    finishAffinity();
                } else {
                    Intent marketActivity = new Intent(LoginRegisterActivity.this, MarketActivityK.class);
                    startActivity(marketActivity);
                    finishAffinity();
                }
                break;
            case REGISTER_EMAIL:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // goes to verification fragment
                setFragment(LoginRegistrationFragments.VERIFY, new Pair<>(requestType, savedRequests.get(requestType).getString("email")));
                break;
            case REGISTER_PHONE:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // goes to verification fragment
                setFragment(LoginRegistrationFragments.VERIFY, new Pair<>(requestType, savedRequests.get(requestType).getString("phone")));
                break;
            case CODE_CONFIRMATION_EMAIL:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // remove saved request for email registration
                savedRequests.remove(RequestType.REGISTER_EMAIL);
                // goes to verification fragment
                setFragment(LoginRegistrationFragments.ADD_PHONE, null);
                break;
            case CODE_CONFIRMATION_PHONE:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // remove saved request for email registration
                savedRequests.remove(RequestType.REGISTER_PHONE);
                // goes to verification fragment
                setFragment(LoginRegistrationFragments.ADD_EMAIL, null);
                break;
            case ADD_EMAIL:
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token")); // TODO check if needed or not
                setFragment(LoginRegistrationFragments.VERIFY, new Pair<>(requestType, savedRequests.get(requestType).getString("mail")));
                break;
            case ADD_PHONE:
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token")); // TODO check if needed or not
                setFragment(LoginRegistrationFragments.VERIFY, new Pair<>(requestType, savedRequests.get(requestType).getString("telephone")));
                break;
            case ADD_EMAIL_CONFIRM:
                //save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // remove already saved request
                savedRequests.remove(RequestType.ADD_EMAIL);
                // sends a new request to check user's condition
                loginRegisterViewModel.setJsonRequest(null, RequestType.USER);
                break;
            case ADD_PHONE_CONFIRM:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                // remove already saved request
                savedRequests.remove(RequestType.ADD_PHONE);
                // sends a new request to check user's condition
                loginRegisterViewModel.setJsonRequest(null, RequestType.USER);
                break;
            case RESEND_REGISTER_EMAIL:
            case RESEND_REGISTER_PHONE:
            case RESEND_ADD_EMAIL:
            case RESEND_ADD_PHONE:
                // save token
                new Utils().saveEncryptedSharedPreferences(this,"token", hashResponse.get("token_type") + " " + hashResponse.get("access_token"));
                loginRegisterViewModel.setRequestResentStatus(true);
                break;
            default:
                throw new IllegalStateException("Unexpected value: Registration Activity, handle succeed response");
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

    private void unauthorizedResponse(RequestType requestType) {
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

        switch (requestType) {
            case LOGIN:
                lblMsgHeader.setText(getResources().getString(R.string.error));
                lblMsg.setText(getResources().getString(R.string.incorrect_username_password));
                break;
            case CODE_CONFIRMATION_EMAIL:
            case CODE_CONFIRMATION_PHONE:
            case ADD_EMAIL_CONFIRM:
            case ADD_PHONE_CONFIRM:
                lblMsgHeader.setText(getResources().getString(R.string.error));
                lblMsg.setText(getResources().getString(R.string.wrong_authentication_code));
                break;
            default:
                lblMsgHeader.setText(getResources().getString(R.string.sessionTimeOut));
                lblMsg.setText(R.string.session_timeout_msg);
                break;
        }

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);

        // show dialog
        dialog.show();
    }

    private LoginRegistrationFragments getActiveFragment() {
        if (loginFragment != null && loginFragment.isVisible()) {
            return LoginRegistrationFragments.LOGIN;
        }
        if (registrationFragment != null && registrationFragment.isVisible()) {
            return LoginRegistrationFragments.REGISTER;
        }
        if (verificationFragment != null && verificationFragment.isVisible()) {
            return LoginRegistrationFragments.VERIFY;
        }
        if (addEmailFragment != null && addEmailFragment.isVisible()) {
            return LoginRegistrationFragments.ADD_EMAIL;
        }
        if (addPhoneFragment != null && addPhoneFragment.isVisible()) {
            return LoginRegistrationFragments.ADD_PHONE;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        LoginRegistrationFragments activeFragment = getActiveFragment();
        if (activeFragment != null) {
            switch (activeFragment) {
                case LOGIN:
                    finishAffinity();
                    break;
                case VERIFY:
                    exitDialog();
                    break;
                default:
                    setFragment(LoginRegistrationFragments.LOGIN, null);
                    break;
            }
        }
    }

    public void exitDialog() {
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
                setFragment(LoginRegistrationFragments.LOGIN, null);
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