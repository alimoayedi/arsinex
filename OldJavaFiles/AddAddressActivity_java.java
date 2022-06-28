package com.arsinex.com.OldJavaFiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Objects.AddressObject;
import com.arsinex.com.Objects.AssetObject;
import com.arsinex.com.Objects.BankObject;
import com.arsinex.com.Objects.NetworkObject;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.withdrawPackage.AddressActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAddressActivity_java extends AppCompatActivity {

    // Constants
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String GET_ASSETS_LIST_TAG = "get_assets";
    private static final String GET_BANKS_LIST_TAG = "get_banks";
    private static final String GET_NETWORKS_LIST_TAG = "get_networks";

    private TextView lblSave, lblWalletAddressHeader;
    private TabLayout tabLyCurrency;
    private Spinner spAssets, spNetwork;
    private TextInputEditText txtWithdrawAddress, txtAddressName;
    private ImageView btnBack, imgQRScanner;

    private ArrayList<AssetObject> fiatList = new ArrayList<AssetObject>();
    private ArrayList<AssetObject> cryptoList = new ArrayList<AssetObject>();
    private ArrayList<NetworkObject> networkList = new ArrayList<NetworkObject>();
    private ArrayList<BankObject> bankList = new ArrayList<BankObject>();

    private ArrayList<String> assetsList_spinner = new ArrayList<String>();
    private ArrayList<String> networkList_spinner = new ArrayList<String>();

    private ArrayAdapter assetAdaptor;
    private ArrayAdapter networkAdaptor;

    private OkHttpClient client = new OkHttpClient();
    private Request request;
    private RequestBody requestBody;

    private Utils utils = new Utils();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        initializeComponents();

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
                    fetchBanksList();
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
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spAssets.getSelectedItemPosition() != 0 || (networkAdaptor.getCount() > 0 && spNetwork.getSelectedItemPosition() != 0) || !txtWithdrawAddress.getText().toString().isEmpty() || !txtAddressName.getText().toString().isEmpty()) {
                    exitWithOutSave();
                } else {
                    onBackPressed();
                }
            }
        });

        lblSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkFormValidity()) { return; } // if any problem exists will not save

                AddressObject address = new AddressObject(
                        System.currentTimeMillis()/1000,
                        tabLyCurrency.getSelectedTabPosition() == 0 ? false : true,
                        spAssets.getSelectedItem().toString(),
                        spNetwork.getSelectedItem().toString(),
                        txtWithdrawAddress.getText().toString(),
                        txtAddressName.getText().toString()
                );

                String key = String.valueOf(address.getSavedTime());

                String stringObject = gson.toJson(address);

                SharedPreferences.Editor editor = getSharedPreferences("user_addresses", MODE_PRIVATE).edit();
                editor.putString(key, stringObject);
                editor.apply();

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void initializeComponents() {
        tabLyCurrency = (TabLayout) findViewById(R.id.tabLyCurrency);
        spAssets = (Spinner) findViewById(R.id.spAssets);
        spNetwork = (Spinner) findViewById(R.id.spNetwork);
        txtWithdrawAddress = (TextInputEditText) findViewById(R.id.txtWithdrawAddress);
        txtAddressName = (TextInputEditText) findViewById(R.id.txtAddressName);
        lblWalletAddressHeader = (TextView) findViewById(R.id.lblWalletAddressHeader);
        lblSave = (TextView) findViewById(R.id.lblSave);
        btnBack = (ImageView) findViewById(R.id.btnBack);
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
    private void fetchBanksList() {
        final String url = new RequestUrls().getUrl(RequestType.GET_BANKS_LIST);
        requestBody = RequestBody.create(new byte[0]);
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_BANKS_LIST_TAG).build();
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
        request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(this, "token")).post(requestBody).tag(GET_NETWORKS_LIST_TAG).build();
        makeApiRequest(RequestType.GET_NETWORKS_LIST, request);
    }

    private void makeApiRequest(RequestType requestType, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        call.cancel();
                        makeApiRequest(requestType, request);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorMessage(response.code());
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
        }
    }

    private void updateSpinners(RequestType requestType) {
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
    private void resetUI() {
        networkAdaptor.clear();
        txtWithdrawAddress.setText("");
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
                    Intent loginActivity = new Intent(AddAddressActivity_java.this, LoginRegisterActivity.class);
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

    private void exitWithOutSave() {
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

        btnPositive.setText(getString(R.string.yes));
        btnNegative.setText(R.string.no);

        lblMsgHeader.setText(R.string.warning);
        lblMsg.setText(R.string.exit_without_saving);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addressActivity = new Intent(AddAddressActivity_java.this, AddressActivity.class); //TODO "WebViewActivity" should change to "MarketActivity"
                setResult(RESULT_CANCELED, addressActivity);
                dialog.dismiss();
                finish();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        // show dialog
        dialog.show();
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
        return true;
    }

}