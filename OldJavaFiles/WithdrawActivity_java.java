package com.arsinex.com.OldJavaFiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Objects.AddressObject;
import com.arsinex.com.Objects.NetworkObject;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.withdrawPackage.AddressActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.arsinex.com.Objects.AssetObject;
import com.arsinex.com.Objects.BankObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.MainActivityUtilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WithdrawActivity_java extends AppCompatActivity {

    private static final String GET_ASSETS_LIST_TAG = "get_assets";
    private static final String GET_BANKS_LIST_TAG = "get_banks";
    private static final String GET_NETWORKS_LIST_TAG = "get_networks";
    private static final String GET_COMMISSION = "get_commission";
    private static final String SEND_SMS_WITHDRAW = "send_sms";
    private static final String SEND_EMAIL_WITHDRAW = "send_email";
    private static final String BALANCE_TAG = "balance";

    private static final String WITHDRAW_MONEY_REQ = "withdraw_money";
    private static final String WITHDRAW_CRYPTO_REQ = "withdraw_crypto";

    private static final int TIMER_MAX = 62000; // 62 sec.

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // main activity layout
    private DrawerLayout drawerLayout;
    private LinearLayout lyProfile, lyMarket, lyWithdraw, lyDeposit, lyDiscover, lyAboutUs, lyContactUs, lySupport, lyLogout;
    private RelativeLayout lyTransactionDetails;
    private ScrollView lyMain;
    private TabLayout tabLyCurrency;
    private Spinner spAssets, spNetwork;
    private TextView lblWithdrawAddress,lblWalletAddressHeader, lblAmount, lblUnit, lblAll, lblTransactionDetails, lblCryptoWithdrawDetails;
    private EditText txtWithdrawAddress, txtAmount;
    private ImageView imgNotebook;
    private Button btnWithdraw;
    private ProgressBar progressLoading;

    // bottom window layout components
    private EditText txtSMSConfirm, txtEmailConfirm, txtGoogleConfirm;
    private Button btnSMSSend, btnEmailSend, btnGoogleSend, btnContinue;
    private ProgressBar progressBar_sms, progressBar_email, progressBar_google;
    private CountDownTimer sms_countDownTimer;
    private CountDownTimer email_countDownTimer;

    private OkHttpClient client = new OkHttpClient();
    private RequestBody requestBody;

    private ArrayList<AssetObject> fiatList = new ArrayList<AssetObject>();
    private ArrayList<AssetObject> cryptoList = new ArrayList<AssetObject>();
    private ArrayList<NetworkObject> networkList = new ArrayList<NetworkObject>();
    private ArrayList<BankObject> bankList = new ArrayList<BankObject>();

    private ArrayList<String> assetsList_spinner = new ArrayList<String>();
    private ArrayList<String> networkList_spinner = new ArrayList<String>();

    private ArrayAdapter assetAdaptor;
    private ArrayAdapter networkAdaptor;

    private String commission;
    private String balance = "0";

    private Utils utils = new Utils();
    private Gson gson = new Gson();

    private ActivityResultLauncher<Intent> getAddressResultLauncher;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        initializeComponents(); // initialize view components
        initializeDrawerMenu(); // initialize drawer menu elements

        setConnectionTimeout();

        lblWithdrawAddress.setAutoSizeTextTypeUniformWithConfiguration(12, 18, 2, TypedValue.COMPLEX_UNIT_SP);
        TransitionManager.beginDelayedTransition(lyMain, new AutoTransition());

        setupAssetAdaptor(); // set the view of spinner dropdown
        assetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAssets.setAdapter(assetAdaptor);

        setupNetworkAdaptor(); // set the view of spinner dropdown
        networkAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNetwork.setAdapter(networkAdaptor);

        // fetches list of assets including fiat (moneys and crypto currencies)
        fetchAssetsList();

        tabLyCurrency.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    lblWalletAddressHeader.setText(R.string.iban_uban_account_number);
                } else {
                    lblWalletAddressHeader.setText(R.string.withdraw_address);
                }
                updateSpinners(RequestType.GET_ASSETS_LIST);
                resetUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        spAssets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    resetUI();
                } else if (tabLyCurrency.getSelectedTabPosition() == 0) {
                    lblUnit.setText(fiatList.get(position - 1).getSymbol());
                    lblAll.setVisibility(View.VISIBLE);
                    fetchBanksList();
                } else if (tabLyCurrency.getSelectedTabPosition() == 1) {
                    lblUnit.setText(cryptoList.get(position - 1).getSymbol());
                    lblAll.setVisibility(View.VISIBLE);
                    String asset_id = cryptoList.get(position - 1).getAssetId();
                    fetchNetworksList(asset_id);
                }
                txtAmount.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!= 0 && tabLyCurrency.getSelectedTabPosition() == 1 && !txtAmount.getText().toString().isEmpty()) {
                    if (!txtAmount.getText().toString().isEmpty()) {
                        fetchCommission();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tabLyCurrency.getSelectedTabPosition() == 1) {
                    if (spNetwork.getSelectedItemPosition() != 0 && !txtAmount.getText().toString().isEmpty()) {
                        fetchCommission();
                    } else if (s.length() == 0) {
                        collapseTransactionDetails();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lblAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lblAll.setClickable(false);
                lblAll.setVisibility(View.INVISIBLE);
                progressLoading.setVisibility(View.VISIBLE);
                balance = "0";
                fetchTotalBalance();
            }
        });

        imgNotebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressBookIntent = new Intent(WithdrawActivity_java.this, AddressActivity.class);
                getAddressResultLauncher.launch(addressBookIntent);
            }
        });

        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtWithdrawAddress.onEditorAction(EditorInfo.IME_ACTION_DONE);
                txtAmount.onEditorAction(EditorInfo.IME_ACTION_DONE);
                if(tabLyCurrency.getSelectedTabPosition() == 0 && checkFormValidity()) {
                    sendMoneyWithdrawRequest();
                }
                if(tabLyCurrency.getSelectedTabPosition() == 1 ) { // && checkFormValidity()
                    openAuthenticationDialog();
                }
            }
        });

        getAddressResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    String stringAddressObject = result.getData().getStringExtra("selectedAddress");

                    Type type = new TypeToken<AddressObject>() {}.getType();
                    AddressObject addressObject = gson.fromJson(stringAddressObject, type);

                    updateWalletAddress(addressObject);
                }
            }
        });
    }

    private void updateWalletAddress(AddressObject addressObject) {

        // if address is for crypto currency
        if (addressObject.isCrypto() && tabLyCurrency.getSelectedTabPosition() == 1) {
            if (spAssets.getSelectedItem().toString().equals(addressObject.getCurrency())) {
                if (spNetwork.getSelectedItem().toString().equals(addressObject.getNetwork())) {
                    txtWithdrawAddress.setText(addressObject.getWalletAddress());
                } else {
                    ArrayAdapter networkAdapter = (ArrayAdapter) spNetwork.getAdapter();
                    int spinnerPosition = networkAdapter.getPosition(addressObject.getNetwork());
                    spNetwork.setSelection(spinnerPosition); //
                    txtWithdrawAddress.setText(addressObject.getWalletAddress());
                    Toast.makeText(this, getResources().getString(R.string.different_networks), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.different_assets), Toast.LENGTH_SHORT).show();
            }
        } else if (!addressObject.isCrypto() && tabLyCurrency.getSelectedTabPosition() == 0) {
            if (spAssets.getSelectedItem().toString().equals(addressObject.getCurrency())) {
                if (spNetwork.getSelectedItem().toString().equals(addressObject.getNetwork())) {
                    txtWithdrawAddress.setText(addressObject.getWalletAddress());
                } else {
                    ArrayAdapter networkAdapter = (ArrayAdapter) spNetwork.getAdapter();
                    int spinnerPosition = networkAdapter.getPosition(addressObject.getNetwork());
                    spNetwork.setSelection(spinnerPosition); //
                    txtWithdrawAddress.setText(addressObject.getWalletAddress());
                    Toast.makeText(this, getResources().getString(R.string.different_networks), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.different_assets), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.different_assets_type), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFormValidity() {
        if(spAssets.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getResources().getString(R.string.no_asset_selected),Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tabLyCurrency.getSelectedTabPosition() == 0 && spNetwork.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getResources().getString(R.string.preferred_bank_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tabLyCurrency.getSelectedTabPosition() == 1 && spNetwork.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getResources().getString(R.string.preferred_network_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtWithdrawAddress.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.address_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.amount_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initializeComponents() {
        lyMain = (ScrollView) findViewById(R.id.lyMain);
        lyProfile = (LinearLayout) drawerLayout.findViewById(R.id.lyProfile);
        lyMarket = (LinearLayout) drawerLayout.findViewById(R.id.lyMarket);
        lyAboutUs = (LinearLayout) drawerLayout.findViewById(R.id.lyAboutUs);
        lyContactUs = (LinearLayout) drawerLayout.findViewById(R.id.lyContactUs);
        lyWithdraw = (LinearLayout) drawerLayout.findViewById(R.id.lyWithdraw);
        lyDeposit = (LinearLayout) drawerLayout.findViewById(R.id.lyDeposit);
        lyDiscover = (LinearLayout) drawerLayout.findViewById(R.id.lyDiscover);
        lySupport = (LinearLayout) drawerLayout.findViewById(R.id.lySupport);
        lyLogout = (LinearLayout) drawerLayout.findViewById(R.id.lyLogout);
        tabLyCurrency = (TabLayout) findViewById(R.id.tabLyCurrency);
        lyTransactionDetails = (RelativeLayout) findViewById(R.id.lyTransactionDetails);
        spAssets = (Spinner) findViewById(R.id.spAssets);
        spNetwork = (Spinner) findViewById(R.id.spNetwork);
        lblWalletAddressHeader = (TextView) findViewById(R.id.lblWalletAddressHeader);
        lblWithdrawAddress = (EditText) findViewById(R.id.txtWithdrawAddress);
        lblAmount = (EditText) findViewById(R.id.txtAmount);
        lblUnit = (TextView) findViewById(R.id.lblUnit);
        lblAll = (TextView) findViewById(R.id.lblAll);
        progressLoading = (ProgressBar) findViewById(R.id.progressLoading);
        lblTransactionDetails = (TextView) findViewById(R.id.lblTransactionDetails);
        lblCryptoWithdrawDetails = (TextView) findViewById(R.id.lblCryptoWithdrawDetails);
        txtAmount = (EditText) findViewById(R.id.txtAmount);
        txtWithdrawAddress = (EditText) findViewById(R.id.txtWithdrawAddress);
        imgNotebook = (ImageView) findViewById(R.id.imgNotebook);
        btnWithdraw = (Button) findViewById(R.id.btnWithdraw);
    }
    private void initializeDrawerMenu() {
        lyMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickMarket(WithdrawActivity_java.this);
            }
        });

        lyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickProfile(WithdrawActivity_java.this);
            }
        });

        lyAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickAboutUs(WithdrawActivity_java.this);
            }
        });

        lyContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickContactUs(WithdrawActivity_java.this);
            }
        });

        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickWithdraw();
            }
        });

        lyDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickDeposit(WithdrawActivity_java.this);
            }
        });

        lyDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickDiscover(WithdrawActivity_java.this);
            }
        });

        lySupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickSupport(WithdrawActivity_java.this);
            }
        });

        lyLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickLogout(WithdrawActivity_java.this);
            }
        });
    }

    private void setupAssetAdaptor() {
        assetAdaptor = new ArrayAdapter(this, android.R.layout.simple_spinner_item, assetsList_spinner) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setGravity(Gravity.LEFT);
                ((TextView) v).setTextSize(getResources().getDimension(R.dimen.base_font_size) / getResources().getDisplayMetrics().density);
                ((TextView) v).setTextColor(getResources().getColor(R.color.mainText, null));
                if (position == 0) {
                    ((TextView) v).setTextColor(getColor(R.color.hintColor));
                } else {
                    ((TextView) v).setTextColor(getColor(R.color.mainText));
                }
                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextSize(getResources().getDimension(R.dimen.small_font_size) / getResources().getDisplayMetrics().density);
                ((TextView) v).setPadding(
                        (int) (getResources().getDimension(R.dimen.padding_left) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_up) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_right) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_bottom) / getResources().getDisplayMetrics().density));

                return v;
            }
        };
    }
    private void setupNetworkAdaptor() {
        networkAdaptor = new ArrayAdapter(this, android.R.layout.simple_spinner_item, networkList_spinner) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setGravity(Gravity.LEFT);
                ((TextView) v).setTextSize(getResources().getDimension(R.dimen.base_font_size) / getResources().getDisplayMetrics().density);
                ((TextView) v).setTextColor(getResources().getColor(R.color.mainText, null));
                if (position == 0) {
                    ((TextView) v).setTextColor(getColor(R.color.hintColor));
                } else {
                    ((TextView) v).setTextColor(getColor(R.color.mainText));
                }
                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextSize(getResources().getDimension(R.dimen.small_font_size) / getResources().getDisplayMetrics().density);
                ((TextView) v).setPadding(
                        (int) (getResources().getDimension(R.dimen.padding_left) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_up) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_right) / getResources().getDisplayMetrics().density),
                        (int) (getResources().getDimension(R.dimen.padding_bottom) / getResources().getDisplayMetrics().density));

                return v;
            }
        };
    }

    private void fetchAssetsList() {
        final String url = new RequestUrls().getUrl(RequestType.GET_ASSETS_LIST);
        requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_ASSETS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_ASSETS_LIST, request);
    }
    private void fetchBanksList() {
        final String url = new RequestUrls().getUrl(RequestType.GET_BANKS_LIST);
        requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_BANKS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_BANKS_LIST, request);
    }
    private void fetchNetworksList(String asset_id) {
        final String url = new RequestUrls().getUrl(RequestType.GET_NETWORKS_LIST);
        JSONObject jsonRequest = null;
        try {
            jsonRequest = new JSONObject().put("asset_id", asset_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_NETWORKS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_NETWORKS_LIST, request);
    }
    private void fetchCommission() {
        if (txtAmount.getText().length() == 0) {
            collapseTransactionDetails();
        }
        JSONObject jsonRequest = null;

        String asset_id = cryptoList.get(spAssets.getSelectedItemPosition() - 1).getAssetId();
        String network_id = networkList.get(spNetwork.getSelectedItemPosition() - 1).getNetwork_id();
        String amount = utils.standardizeTurkishStrings(txtAmount.getText().toString());

        try {
            jsonRequest = new JSONObject()
                    .put("asset_id", asset_id)
                    .put("network_id", network_id)
                    .put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        final String url = new RequestUrls().getUrl(RequestType.GET_COMMISSION);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_COMMISSION).build();
        makeApiRequest(RequestType.GET_COMMISSION, request);
    }
    private void fetchTotalBalance() {
        final String url = new RequestUrls().getUrl(RequestType.WALLET_BALANCE);
        requestBody = RequestBody.create(new byte[0]);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(BALANCE_TAG).build();
        makeApiRequest(RequestType.WALLET_BALANCE, request);
    }

    private void setConnectionTimeout() {
        client.newBuilder()
                .connectTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(new ConnectionSettings().CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public void makeApiRequest(RequestType requestType, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        call.cancel();
                        failureRequest(requestType, request);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                if (response.code() == HttpsURLConnection.HTTP_OK) {
                    String stringResponse = response.body().string();
                    checkResponseStatus(stringResponse, requestType);
                } else {
                    showErrorMessage(response.code());
                }
                response.close();
            }
        });
    }

    private void checkResponseStatus(String response, RequestType requestType) {
        if(this != null && !isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handleSuccessResponse(response, requestType);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void handleSuccessResponse(String response, RequestType requestType) throws JSONException {
        Log.d("*************************************", response);
        switch (requestType){
            case GET_ASSETS_LIST:
            case GET_BANKS_LIST:
            case GET_NETWORKS_LIST:
                parseResponse(requestType, response);
                updateSpinners(requestType);
                break;
            case WITHDRAW_MONEY_REQUEST:
                boolean success = new JSONObject(response).getBoolean("success");
                // if withdrawing is unsuccessful gets msg from the server and shows it to user!
                String msg = success ? getSuccessfulWithdrawMsg(new JSONObject(response).getJSONObject("data")) : new JSONObject(response).getString("message");
                showMessage(msg);
                break;
            case GET_COMMISSION:
                updateTransactionDetails(new JSONObject(response));
                break;
            case SEND_SMS_FOR_WITHDRAW:
                if (new JSONObject(response).getBoolean("success")) {
                    start_smsTimer();
                } else {
                    btnSMSSend.setEnabled(true);
                    showMessage(new JSONObject(response).getString("message"));
                }
                break;
            case SEND_EMAIL_FOR_WITHDRAW:
                if (new JSONObject(response).getBoolean("success")) {
                    start_emailTimer();
                } else {
                    btnEmailSend.setEnabled(true);
                    showMessage(new JSONObject(response).getString("message"));
                }
                break;
            case WITHDRAW_CRYPTO_REQUEST:
                showMessage(new JSONObject(response).getString("message"));
                break;
            case WALLET_BALANCE:
                parseResponse(requestType, response);
                updateAmountField();
                break;
            default:
                throw new IllegalArgumentException("Invalid Key, request type withdraw activity");
        }
    }

    private void parseResponse(RequestType requestType, String response) throws JSONException {
        JSONArray jsonArray = null;
        switch (requestType) {
            case GET_ASSETS_LIST:
                fiatList.clear();
                jsonArray = new JSONObject(response).getJSONArray("fiat_assets");
                for (int INDEX = 0; INDEX<jsonArray.length() ; INDEX++) {
                    JSONObject entry = jsonArray.getJSONObject(INDEX);
                    fiatList.add(new AssetObject(
                            entry.getString("id"),
                            entry.getString("title"),
                            entry.getString("symbol"),
                            entry.getString("asset_type"),
                            entry.getString("mobile_logo")
                    ));
                }
                cryptoList.clear();
                jsonArray = new JSONObject(response).getJSONArray("crypto_assets");
                for (int INDEX = 0 ; INDEX<jsonArray.length() ; INDEX++) {
                    JSONObject entry = jsonArray.getJSONObject(INDEX);
                    cryptoList.add(new AssetObject(
                            entry.getString("id"),
                            entry.getString("title"),
                            entry.getString("symbol"),
                            entry.getString("asset_type"),
                            entry.getString("mobile_logo")
                    ));
                }
                break;
            case GET_BANKS_LIST:
                bankList.clear();
                jsonArray = new JSONObject(response).getJSONArray("banks");
                for (int INDEX = 0 ; INDEX<jsonArray.length() ; INDEX++) {
                    JSONObject entry = jsonArray.getJSONObject(INDEX);
                    bankList.add(new BankObject(
                            entry.getString("id"),
                            entry.getString("title"),
                            entry.getString("type"),
                            entry.getString("working_hours"),
                            entry.getString("logo")
                    ));
                }
                break;
            case GET_NETWORKS_LIST:
                networkList.clear();
                jsonArray = new JSONObject(response).getJSONArray("tokens");
                for (int INDEX=0 ; INDEX < jsonArray.length() ; INDEX ++) {
                    JSONObject entry = jsonArray.getJSONObject(INDEX);
                    networkList.add(new NetworkObject(
                            entry.getString("asset_id"),
                            entry.getString("network_id"),
                            entry.getString("network_name"),
                            entry.getString("contract_address"),
                            entry.getString("comission")
                    ));
                }
                break;
            case WALLET_BALANCE:
                if(tabLyCurrency.getSelectedTabPosition() == 0) { // if currency selected
                    String symbol = fiatList.get(spAssets.getSelectedItemPosition()-1).getSymbol();
                    JSONObject all_balances = new JSONObject(response).getJSONObject("balances").getJSONObject("fiat_balances").getJSONObject(symbol);
                    if (all_balances.has(symbol)) {
                        balance = all_balances.getString(symbol);
                    }
                } else {
                    String symbol = cryptoList.get(spAssets.getSelectedItemPosition()-1).getSymbol();
                    JSONObject all_balances = new JSONObject(response).getJSONObject("balances").getJSONObject("balances").getJSONObject(symbol);
                    if (all_balances.has("available")) {
                        balance = all_balances.getString("available");
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid Key, request type withdraw activity");

        }
    }

    public void updateSpinners(RequestType requestType) {
        switch (requestType) {
            case GET_ASSETS_LIST:
                if (tabLyCurrency.getSelectedTabPosition() == 0) {
                    setupCurrencySpinner();
                } else {
                    setupCryptoSpinner();
                }
                break;
            case GET_NETWORKS_LIST:
                setupNetworkSpinner();
                break;
            case GET_BANKS_LIST:
                setupBankSpinner();
                break;
        }
    }

    private void setupCurrencySpinner() {
        assetsList_spinner.clear();
        assetsList_spinner.add(getResources().getString(R.string.choose_currency));
        for(AssetObject assetObject:fiatList) {
            assetsList_spinner.add(assetObject.getAssetTitle());
        }
        spAssets.setSelection(0);
        assetAdaptor.notifyDataSetChanged();
    }
    private void setupCryptoSpinner() {
        assetsList_spinner.clear();
        assetsList_spinner.add(getResources().getString(R.string.choose_crypto));
        for(AssetObject assetObject:cryptoList) {
            assetsList_spinner.add(assetObject.getAssetTitle());
        }
        spAssets.setSelection(0);
        assetAdaptor.notifyDataSetChanged();
    }
    private void setupNetworkSpinner() {
        networkList_spinner.clear();
        networkList_spinner.add(getResources().getString(R.string.choose_network));
        for(NetworkObject networkObject:networkList) {
            networkList_spinner.add(networkObject.getNetwork_name());
        }
        spNetwork.setSelection(0);
        networkAdaptor.notifyDataSetChanged();
    }
    private void setupBankSpinner() {
        networkList_spinner.clear();
        networkList_spinner.add(getResources().getString(R.string.choose_bank));
        for(BankObject bankObject:bankList) {
            networkList_spinner.add(bankObject.getTitle());
        }
        spNetwork.setSelection(0);
        networkAdaptor.notifyDataSetChanged();
    }

    private void updateAmountField(){
        txtAmount.setText(balance);
        progressLoading.setVisibility(View.INVISIBLE);
        lblAll.setVisibility(View.VISIBLE);
        lblAll.setClickable(true);
    }

    private void sendMoneyWithdrawRequest() {
        String asset_id = fiatList.get(spAssets.getSelectedItemPosition() - 1).getAssetId();
        String bank_id = bankList.get(spNetwork.getSelectedItemPosition() - 1).getBankId();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest
                    .put("asset_id", asset_id)
                    .put("banka_id", bank_id)
                    .put("hesap_numarasi", txtWithdrawAddress.getText().toString())
                    .put("amount", utils.standardizeTurkishStrings(txtAmount.getText().toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        final String url = new RequestUrls().getUrl(RequestType.WITHDRAW_MONEY_REQUEST);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(WITHDRAW_MONEY_REQ).build();
        makeApiRequest(RequestType.WITHDRAW_MONEY_REQUEST, request);
    }

    private void showMessage(String msg) {
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
        lblMsgHeader.setText(getResources().getString(R.string.withdraw));
        btnPositive.setText(getString(R.string.ok));

        lblMsg.setText(msg);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        // show dialog
        dialog.show();
    }

    private void showErrorMessage(int http_code) {
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
        btnPositive.setText(getString(R.string.ok));

        if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED){
            lblMsgHeader.setText(getResources().getString(R.string.sessionTimeOut));
            lblMsg.setText(R.string.session_timeout_msg);
        } else {
            lblMsgHeader.setText(getResources().getString(R.string.error));
            lblMsg.setText(String.valueOf(http_code));
        }

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    Intent loginActivity = new Intent(WithdrawActivity_java.this, LoginRegisterActivity.class);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginActivity);
                    finishAffinity();
                }
            }
        });
        // show dialog
        dialog.show();
    }

    private void failureRequest(RequestType requestType, Request request){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // create alert dialog
        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
        btnNegative.setVisibility(View.GONE); // No need for this button here!
        lblMsgHeader.setText(getResources().getString(R.string.error));
        lblMsg.setText(getResources().getString(R.string.server_connection_failure));
        btnPositive.setText(getResources().getString(R.string.try_again));
        dialog.setCancelable(true);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                makeApiRequest(requestType, request);
            }
        });
        dialog.show();
    }

    private String getSuccessfulWithdrawMsg (JSONObject json_server_msg) throws JSONException {
        String account_number;

        if (json_server_msg.has("iban")) {
            account_number = json_server_msg.getString("iban");
        } else if (json_server_msg.has("uban")) {
            account_number = json_server_msg.getString("uban");
        } else {
            account_number = json_server_msg.getString("account_number");
        }

        return getResources().getString(R.string.withdraw_successful)
                + "\n\n"                                                        // leaves 2 lines empty
                + spNetwork.getSelectedItem().toString() + "\n"                 // shows selected bank
                + account_number + "\n"                // shows IBAN or account number
                + spAssets.getSelectedItem().toString() + "\n"                  // shows currency
                + json_server_msg.getString("amount")                     // shows withdraw amount
                + " "                                                           // adds space between value and symbol
                + getMoneySymbol(json_server_msg.getInt("asset_id"));     // shows currency symbol
    }

    private String getMoneySymbol(int asset_id) {
        String symbol = "";
        switch (asset_id) {
            case 2:
                symbol = "₺";
                break;
            case 3:
                symbol = "$";
                break;
            case 4:
                symbol = "€";
                break;
            case 5:
                symbol = "£";
                break;
        }
        return symbol;
    }

    private void resetUI() {
        networkAdaptor.clear();
        txtWithdrawAddress.setText("");
        txtAmount.setText("");
        lblUnit.setText("");
        lblAll.setVisibility(View.INVISIBLE);
        progressLoading.setVisibility(View.INVISIBLE);
        collapseTransactionDetails();
    }

    private void updateTransactionDetails(JSONObject response) throws JSONException {
        TransitionManager.beginDelayedTransition(lyTransactionDetails, new AutoTransition());
        lyTransactionDetails.setVisibility(View.VISIBLE);

        if(response.has("status")) {
            lblCryptoWithdrawDetails.setText(response.getString("message"));
        } else {
            NumberFormat formatter = new DecimalFormat("#0.00000000");
            commission = formatter.format(Double.parseDouble(response.getString("commission_rate")));
            lblCryptoWithdrawDetails.setText(R.string.commission);
            String details = lblCryptoWithdrawDetails.getText().toString()
                    + ": "
                    + commission
                    + "\t"
                    + spAssets.getSelectedItem().toString();
            lblCryptoWithdrawDetails.setText(details);
        }
    }

    private void collapseTransactionDetails() {
        TransitionManager.beginDelayedTransition(lyTransactionDetails, new AutoTransition());
        lyTransactionDetails.setVisibility(View.GONE);
    }

    private void openAuthenticationDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WithdrawActivity_java.this, R.style.Theme_Design_BottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.withdraw_confirmation_bottom_sheet, (LinearLayout)findViewById(R.id.bottomSheetContainer));

        txtSMSConfirm = (EditText) bottomSheetView.findViewById(R.id.txtSMSConfirm);
        txtEmailConfirm = (EditText) bottomSheetView.findViewById(R.id.txtEmailConfirm);
//        txtGoogle = (EditText) bottomSheetView.findViewById(R.id.txtGoogleConfirm);

        btnSMSSend = (Button) bottomSheetView.findViewById(R.id.btnSMSSend);
        btnEmailSend = (Button) bottomSheetView.findViewById(R.id.btnEmailSend);
//        btnGoogleSend = (Button) bottomSheetView.findViewById(R.id.btnGoogleSend);

        progressBar_sms = (ProgressBar) bottomSheetView.findViewById(R.id.progressbarSms);
        progressBar_email = (ProgressBar) bottomSheetView.findViewById(R.id.progressbarEmail);
//        progressBar_google = (ProgressBar) bottomSheetView.findViewById(R.id.progressbar_google);

        btnContinue = (Button) bottomSheetView.findViewById(R.id.btnContinue);

        progressBar_sms.setMax(TIMER_MAX);
        progressBar_email.setMax(TIMER_MAX);
//        progressBar_google.setMax(TIMER_MAX);

        btnSMSSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSMSSend.setEnabled(false);
                requestBody = RequestBody.create(new byte[0]);
                final String url = new RequestUrls().getUrl(RequestType.SEND_SMS_FOR_WITHDRAW);
                Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(WithdrawActivity_java.this, "token")).post(requestBody).tag(SEND_SMS_WITHDRAW).build();
                makeApiRequest(RequestType.SEND_SMS_FOR_WITHDRAW, request);
            }
        });

        btnEmailSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEmailSend.setEnabled(false);
                requestBody = RequestBody.create(new byte[0]);
                final String url = new RequestUrls().getUrl(RequestType.SEND_EMAIL_FOR_WITHDRAW);
                Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(WithdrawActivity_java.this, "token")).post(requestBody).tag(SEND_EMAIL_WITHDRAW).build();
                makeApiRequest(RequestType.SEND_EMAIL_FOR_WITHDRAW, request);
            }
        });

//        btnGoogleSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtSMSConfirm.getText().toString().length() != 6 || txtEmailConfirm.getText().toString().length() != 6) {
                    Toast.makeText(WithdrawActivity_java.this, R.string.wrong_authentication_code, Toast.LENGTH_SHORT).show();
                } else {
                    bottomSheetDialog.cancel();
                    sendCryptoWithdrawRequest();
                }
            }
        });

        bottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                sms_countDownTimer.cancel();
//                email_countDownTimer.cancel();
            }
        });

        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (progressBar_sms.getProgress() != 0) {
                    btnSMSSend.setEnabled(false);
                }
                if (progressBar_email.getProgress() != 0) {
                    btnEmailSend.setEnabled(false);
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void start_smsTimer() {
        sms_countDownTimer = new CountDownTimer(TIMER_MAX, 1) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                progressBar_sms.setProgress((int) (leftTimeInMilliseconds));
            }

            @Override
            public void onFinish() {
                btnSMSSend.setEnabled(true);
                cancel();
            }
        }.start();
    }

    private void start_emailTimer() {
        email_countDownTimer = new CountDownTimer(TIMER_MAX, 1) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                progressBar_email.setProgress((int) (leftTimeInMilliseconds));
            }

            @Override
            public void onFinish() {
                btnEmailSend.setEnabled(true);
                cancel();
            }
        }.start();
    }

    private void sendCryptoWithdrawRequest() {
        String asset_id = cryptoList.get(spAssets.getSelectedItemPosition() - 1).getAssetId();
        String network_id = networkList.get(spNetwork.getSelectedItemPosition() - 1).getNetwork_id();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest
                    .put("asset_id", asset_id)
                    .put("network_id", network_id)
                    .put("amount", utils.standardizeTurkishStrings(txtAmount.getText().toString()))
                    .put("cekim_adresi", txtWithdrawAddress.getText().toString())
                    .put("sms_kodu", txtSMSConfirm.getText().toString())
                    .put("eposta_kodu", txtEmailConfirm.getText().toString())
                    .put("tfa_kodu", "")
                    .put("commission", commission);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        final String url = new RequestUrls().getUrl(RequestType.WITHDRAW_CRYPTO_REQUEST);
        Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(WITHDRAW_CRYPTO_REQ).build();
        makeApiRequest(RequestType.WITHDRAW_CRYPTO_REQUEST, request);
    }


    public void ClickWithdraw() {
        // Recreate activity
        MainActivityUtilities.closeDrawer(drawerLayout);
    }
    public void ClickMenu(View view) {
        //Open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout);
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