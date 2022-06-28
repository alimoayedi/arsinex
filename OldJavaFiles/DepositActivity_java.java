package com.arsinex.com.OldJavaFiles;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.arsinex.com.ConnectionSettings;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Objects.NetworkObject;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.enums.RequestType;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.arsinex.com.Objects.AssetObject;
import com.arsinex.com.Objects.BankObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.MainActivityUtilities;
import com.arsinex.com.Utilities.Utils;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DepositActivity_java extends AppCompatActivity {

    private static final int QR_SIZE = 650;

    private static final String GET_ASSETS_LIST_TAG = "get_assets";
    private static final String GET_DEPOSIT_BANK_ACCOUNTS_TAG = "get_accounts";
    private static final String GET_NETWORKS_LIST_TAG = "get_networks";
    private static final String GET_WALLET_ADDRESS_TAG = "get_wallet";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private DrawerLayout drawerLayout;
    private TextView lblWalletAddress, lblBankName, lblAccountName, lblAccountNumber, lblIban, lblDesc;
    private ScrollView lyMain;
    private LinearLayout lyProfile, lyMarket, lyAboutUs, lyWithdraw, lyDeposit, lyDiscover, lyContactUs, lySupport, lyLogout, lyWalletAddress;
    private CardView cardBankAccount;
    private ImageView imgQRCode, imgCopyAccName,imgCopyAccNum,imgCopyIban, imgCopyDesc;
    private TabLayout tabLyCurrency;
    private Spinner spAssets, spNetwork;
    private Button btnCopyAddress, btnShareAddress;

    private OkHttpClient client = new OkHttpClient();
    private Request request;
    private RequestBody requestBody;

    private ArrayList<AssetObject> fiatList = new ArrayList<AssetObject>();
    private ArrayList<AssetObject> cryptoList = new ArrayList<AssetObject>();
    private ArrayList<NetworkObject> networkList = new ArrayList<NetworkObject>();
    private ArrayList<BankObject> bankList = new ArrayList<BankObject>();

    private ArrayList<String> assetsList_spinner = new ArrayList<String>();
    private ArrayList<String> networkList_spinner = new ArrayList<String>();

    private ArrayAdapter assetAdaptor;
    private ArrayAdapter networkAdaptor;

    private String user_wallet_desc = null;

    private Utils utils = new Utils();

    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        initializeComponents();
        initializeDrawerMenu();
        setConnectionTimeout();

        lblWalletAddress.setAutoSizeTextTypeUniformWithConfiguration(12, 18, 2, TypedValue.COMPLEX_UNIT_SP);
        TransitionManager.beginDelayedTransition(lyMain, new AutoTransition());

        setupAssetAdaptor(); // set the view of spinner dropdown
        assetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAssets.setAdapter(assetAdaptor);

        setupNetworkAdaptor(); // set the view of spinner dropdown
        networkAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNetwork.setAdapter(networkAdaptor);

        cardBankAccount.setVisibility(View.GONE);
        lyWalletAddress.setVisibility(View.GONE);
        btnCopyAddress.setVisibility(View.GONE);
        btnShareAddress.setVisibility(View.GONE);

        // set default as the money deposit and fetch currency list
        fetchAssetsList();

        tabLyCurrency.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateSpinners(RequestType.GET_ASSETS_LIST);
                reset_network_spinner();
                resetUI();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        spAssets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    reset_network_spinner();
                    resetUI();
                } else if (tabLyCurrency.getSelectedTabPosition() == 0) {
                    String asset_id = fiatList.get(position - 1).getAssetId();
                    fetchBanksList(asset_id);
                } else if (tabLyCurrency.getSelectedTabPosition() == 1) {
                    String asset_id = cryptoList.get(position - 1).getAssetId();
                    fetchNetworksList(asset_id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    resetUI();
                } else if (tabLyCurrency.getSelectedTabPosition() == 0) {
                    displaySelectedBankDetails(bankList.get(position-1));
                } else if (tabLyCurrency.getSelectedTabPosition() == 1) {
                    fetchWalletAddress(networkList.get(position-1).getNetwork_id());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imgCopyAccName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acc_name = lblAccountName.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.account_name), acc_name);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.acc_name_copied), Toast.LENGTH_SHORT).show();
            }
        });

        imgCopyAccNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acc_name = lblAccountNumber.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.account_number), acc_name);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.acc_num_copied), Toast.LENGTH_SHORT).show();
            }
        });

        imgCopyIban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acc_name = lblIban.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.iban_uban), acc_name);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.iban_copied), Toast.LENGTH_SHORT).show();
            }
        });

        imgCopyDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acc_name = lblAccountName.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.description), acc_name);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.desc_copied), Toast.LENGTH_SHORT).show();
            }
        });

        btnCopyAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = null;
                if (tabLyCurrency.getSelectedTabPosition() == 0) {
                    address = getAccountInfo();
                    Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.acc_info_copied), Toast.LENGTH_SHORT).show();
                } else if (tabLyCurrency.getSelectedTabPosition() == 1) {
                    address = lblWalletAddress.getText().toString();
                    Toast.makeText(DepositActivity_java.this, getResources().getString(R.string.wallet_address_copied), Toast.LENGTH_SHORT).show();
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.wallet_address), address);
                clipboard.setPrimaryClip(clip);
            }
        });

        btnShareAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                if(tabLyCurrency.getSelectedTabPosition() == 0){
                    String title = getResources().getString(R.string.acc_info);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getAccountInfo());
                    startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_acc_info)));
                } else {
                    String title = getResources().getString(R.string.wallet_address);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, lblWalletAddress.getText());
                    startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_address_with)));
                }
            }
        });
    }

    private void initializeComponents() {
        drawerLayout = findViewById(R.id.drawerMenu);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //Assign Variables
        lblWalletAddress = (TextView) findViewById(R.id.lblWalletAddress);
        lyWalletAddress = (LinearLayout) findViewById(R.id.lyWalletAddress);
        lyMain = (ScrollView) findViewById(R.id.lyMain);
        lyProfile = (LinearLayout) drawerLayout.findViewById(R.id.lyProfile);
        lyMarket = (LinearLayout) drawerLayout.findViewById(R.id.lyMarket);
        lyWithdraw = (LinearLayout) drawerLayout.findViewById(R.id.lyWithdraw);
        lyDeposit = (LinearLayout) drawerLayout.findViewById(R.id.lyDeposit);
        lyDiscover = (LinearLayout) drawerLayout.findViewById(R.id.lyDiscover);
        lySupport = (LinearLayout) drawerLayout.findViewById(R.id.lySupport);
        lyAboutUs = (LinearLayout) drawerLayout.findViewById(R.id.lyAboutUs);
        lyContactUs = (LinearLayout) drawerLayout.findViewById(R.id.lyContactUs);
        lyLogout = (LinearLayout) drawerLayout.findViewById(R.id.lyLogout);
        tabLyCurrency = (TabLayout) findViewById(R.id.tabLyCurrency);
        cardBankAccount = (CardView) findViewById(R.id.cardBankAccount);
        lblBankName = (TextView) findViewById(R.id.lblBankName);
        lblAccountName = (TextView) findViewById(R.id.lblAccountName);
        lblAccountNumber = (TextView) findViewById(R.id.lblAccountNumber);
        lblIban = (TextView) findViewById(R.id.lblIban);
        lblDesc = (TextView) findViewById(R.id.lblDesc);
        imgCopyAccName = (ImageView) findViewById(R.id.imgCopyAccName);
        imgCopyAccNum = (ImageView) findViewById(R.id.imgCopyAccNum);
        imgCopyIban = (ImageView) findViewById(R.id.imgCopyIban);
        imgCopyDesc = (ImageView) findViewById(R.id.imgCopyDesc);
        spAssets = (Spinner) findViewById(R.id.spAssets);
        spNetwork = (Spinner) findViewById(R.id.spNetwork);
        imgQRCode = (ImageView) findViewById(R.id.imgQRCode);
        btnCopyAddress = (Button) findViewById(R.id.btnCopyAddress);
        btnShareAddress = (Button) findViewById(R.id.btnShareAddress);
    }
    private void initializeDrawerMenu() {
        lyMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickMarket(DepositActivity_java.this);
            }
        });

        lyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickProfile(DepositActivity_java.this);
            }
        });

        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickWithdraw(DepositActivity_java.this);
            }
        });

        lyDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickDeposit();
            }
        });

        lyDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickDiscover(DepositActivity_java.this);
            }
        });

        lyAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickAboutUs(DepositActivity_java.this);
            }
        });

        lyContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickContactUs(DepositActivity_java.this);
            }
        });

        lySupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickSupport(DepositActivity_java.this);
            }
        });

        lyLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickLogout(DepositActivity_java.this);
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
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_ASSETS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_ASSETS_LIST, request);
    }
    private void fetchBanksList(String asset_id) {
        final String url = new RequestUrls().getUrl(RequestType.GET_DEPOSIT_BANK_ACCOUNTS);
        JSONObject jsonRequest = null;
        try {
            jsonRequest = new JSONObject().put("asset_id", asset_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_DEPOSIT_BANK_ACCOUNTS_TAG).build();
        makeApiRequest(RequestType.GET_DEPOSIT_BANK_ACCOUNTS, request);
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
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_NETWORKS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_NETWORKS_LIST, request);
    }
    private void fetchWalletAddress(String network_id) {
        final String url = new RequestUrls().getUrl(RequestType.GET_WALLET_ADDRESS);
        JSONObject jsonRequest = null;
        try {
            jsonRequest = new JSONObject().put("asset_id", network_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_WALLET_ADDRESS_TAG).build();
        makeApiRequest(RequestType.GET_WALLET_ADDRESS, request);
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
                Log.d("************************", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO
                    }
                });
            }
            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                checkResponseStatus(response, requestType);
            }
        });
    }

    private void checkResponseStatus(Response response, RequestType requestType) {
        if (response.code() == HttpsURLConnection.HTTP_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handleSuccessResponse(response.body().string(), requestType);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        response.body().close();
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorMessage(response.code());
                    response.body().close();
                }
            });
        }
    }

    private void handleSuccessResponse(String response, RequestType requestType) throws JSONException {
        switch (requestType) {
            case GET_ASSETS_LIST:
            case GET_DEPOSIT_BANK_ACCOUNTS:
            case GET_NETWORKS_LIST:
                parseResponse(requestType, response);
                updateSpinners(requestType);
                break;
            case GET_WALLET_ADDRESS:
                displayWalletDetails(response);
                break;
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
            case GET_DEPOSIT_BANK_ACCOUNTS:
                bankList.clear();
                jsonArray = new JSONObject(response).getJSONArray("banks");
                for (int INDEX = 0 ; INDEX<jsonArray.length() ; INDEX++) {
                    JSONObject entry = jsonArray.getJSONObject(INDEX);
                    BankObject bankObject = new BankObject(
                            entry.getJSONObject("bank").getString("id"),
                            entry.getJSONObject("bank").getString("title"),
                            entry.getJSONObject("bank").getString("type"),
                            entry.getJSONObject("bank").getString("working_hours"),
                            entry.getJSONObject("bank").getString("logo")
                    );
                    bankObject.setAccountName(entry.getString("account_name"));
                    bankObject.setAccountIban(entry.getString("account_iban"));
                    bankObject.setAccountNumber(entry.getString("account_number"));
                    bankList.add(bankObject);
                }
                user_wallet_desc = new JSONObject(response).getJSONObject("user_wallets").getString("desc");
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
            case GET_DEPOSIT_BANK_ACCOUNTS:
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
    private void setupBankSpinner() {
        networkList_spinner.clear();
        networkList_spinner.add(getResources().getString(R.string.choose_bank));
        for(BankObject bankObject:bankList) {
            networkList_spinner.add(bankObject.getTitle());
        }
        spNetwork.setSelection(0);
        networkAdaptor.notifyDataSetChanged();
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

    private void displaySelectedBankDetails(BankObject selectedBank) {
        // set text of bank info card
        lblBankName.setText(selectedBank.getTitle());
        lblAccountName.setText(selectedBank.getAccountName());
        lblAccountNumber.setText(selectedBank.getAccountNumber());
        lblIban.setText(selectedBank.getAccountIban());
        lblDesc.setText(user_wallet_desc);

        // make bank account card and buttons visible
        cardBankAccount.setVisibility(View.VISIBLE);
        btnCopyAddress.setVisibility(View.VISIBLE);
        btnShareAddress.setVisibility(View.VISIBLE);
    }

    private void displayWalletDetails(String response) throws JSONException {
        String wallet_address = new JSONObject(response).getJSONObject("wallet").getString("address");
        lblWalletAddress.setText(wallet_address);
        setQRCode(wallet_address);
        lyWalletAddress.setVisibility(View.VISIBLE);
        btnCopyAddress.setVisibility(View.VISIBLE);
        btnShareAddress.setVisibility(View.VISIBLE);
    }

    public void setQRCode(String wallet_address){
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean night_mode = sharedPreferences.getBoolean("night_mode",false);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(wallet_address, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            Bitmap bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGBA_F16);
            for (int x = 0; x<QR_SIZE; x++){
                for (int y=0; y<QR_SIZE; y++){
                    if (night_mode) {
                        bitmap.setPixel(x, y, bitMatrix.get(x,y)? Color.WHITE : ContextCompat.getColor(this, R.color.main_BG));
                    } else {
                        bitmap.setPixel(x, y, bitMatrix.get(x,y)? Color.BLACK : ContextCompat.getColor(this, R.color.main_BG));
                    }
                }
            }
            imgQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset_network_spinner() {
        networkAdaptor.clear();
    }

    public void resetUI() {
        cardBankAccount.setVisibility(View.GONE);
        lyWalletAddress.setVisibility(View.GONE);
        btnCopyAddress.setVisibility(View.GONE);
        btnShareAddress.setVisibility(View.GONE);
        if (tabLyCurrency.getSelectedTabPosition() == 0) {
            btnCopyAddress.setText(getResources().getString(R.string.copy_acc_info));
            btnShareAddress.setText(getResources().getString(R.string.share_acc_info));
        } else {
            btnCopyAddress.setText(getResources().getString(R.string.copy_address));
            btnShareAddress.setText(getResources().getString(R.string.share_address));
        }
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
                    Intent loginActivity = new Intent(DepositActivity_java.this, LoginRegisterActivity.class);
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

    private String getAccountInfo( ){
        String bankInfo = lblBankName.getText().toString()
                + "\n" + getResources().getString(R.string.account_name) + " " + lblAccountName.getText().toString()
                + "\n" + getResources().getString(R.string.account_number) + " " + lblAccountNumber.getText().toString()
                + "\n" + getResources().getString(R.string.iban_uban) + " " + lblIban.getText().toString()
                + "\n" + getResources().getString(R.string.description) + ":" + lblDesc.getText().toString();
        return bankInfo;
    }

    public void ClickDeposit() {
        // Recreate activity
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    public void ClickMenu(View view) {
        //Open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}