package com.arsinex.com.Balance;

import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arsinex.com.marketPackage.MarketViewModel;
import com.arsinex.com.Objects.BalanceCreditObject;
import com.arsinex.com.Objects.BalanceCryptoObject;
import com.arsinex.com.R;
import com.arsinex.com.RequestUrls;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.depositPackage.DepositActivity;
import com.arsinex.com.enums.Currencies;
import com.arsinex.com.enums.RequestType;
import com.arsinex.com.withdrawPackage.WithdrawActivity;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Request;
import okhttp3.RequestBody;

public class BalanceFragment extends Fragment {

    // tags
    private static final String TAG = "************* Balance Fragment *************";
    private static final String NET_WORTH_TAG = "net_worth";
    private static final String BALANCE_TAG = "balance";

    private SwipeRefreshLayout lySwipeRefresh;
    private NestedScrollView lyNestedScroll;
    private TabLayout currencyTabs;
    private RecyclerView recycleCredit, recycleCrypto;
    private BalanceCreditAdaptor balanceCreditAdaptor;
    private BalanceCryptoAdaptor balanceCryptoAdaptor;
    private ProgressBar progressLoadingCredit, progressLoadingCrypto;
    private TextView lblBalance, lblCurrencyUnit, lblHideCredit,lblHideZeroCrypto, lblDeposit, lblWithdraw;
    private Switch switchHideZeroCrypto;

    private Utils utils = new Utils();

    private JSONObject credit_json_object = new JSONObject();
    private JSONObject crypto_json_object = new JSONObject();

    private ArrayList<BalanceCreditObject> moneyList = new ArrayList<BalanceCreditObject>();
    private ArrayList<BalanceCryptoObject> cryptoList_backup = new ArrayList<BalanceCryptoObject>();
    private ArrayList<BalanceCryptoObject> cryptoList = new ArrayList<BalanceCryptoObject>();

    private Boolean balanceHidden = false;
    private Boolean filterIsOn = false;

    private MarketViewModel marketViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View balanceView = inflater.inflate(R.layout.fragment_balance, container, false);

        marketViewModel = new ViewModelProvider(requireActivity()).get(MarketViewModel.class);

        // set toolbar color
        marketViewModel.setToolbarColor(ContextCompat.getColor(balanceView.getContext(), R.color.transparent));

        return balanceView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        currencyTabs = (TabLayout) view.findViewById(R.id.currencyTabs);
        lySwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.lySwipeRefresh);
        lyNestedScroll = (NestedScrollView) view.findViewById(R.id.lyNestedScroll);
        lblBalance = (TextView) view.findViewById(R.id.lblBalance);
        lblCurrencyUnit = (TextView) view.findViewById(R.id.lblCurrencyUnit);
        recycleCredit = (RecyclerView) view.findViewById(R.id.recycleCredit);
        recycleCrypto = (RecyclerView) view.findViewById(R.id.recycleCrypto);
        progressLoadingCrypto = (ProgressBar) view.findViewById(R.id.progressLoadingCrypto);
        progressLoadingCredit = (ProgressBar) view.findViewById(R.id.progressLoadingCredit);
        lblHideCredit = (TextView) view.findViewById(R.id.lblHideCredit);
        lblHideZeroCrypto = (TextView) view.findViewById(R.id.lblHideZeroCrypto);
        switchHideZeroCrypto = (Switch) view.findViewById(R.id.switchHideZeroCrypto);
        lblDeposit = (TextView) view.findViewById(R.id.lblDeposit);
        lblWithdraw = (TextView) view.findViewById(R.id.lblWithdraw);

        progressLoadingCredit.setVisibility(View.VISIBLE);
        progressLoadingCrypto.setVisibility(View.VISIBLE);

        TransitionManager.beginDelayedTransition(recycleCredit, new AutoTransition());
        TransitionManager.beginDelayedTransition(recycleCrypto, new AutoTransition());

        LinearLayoutManager layoutManager_credit = new LinearLayoutManager(view.getContext());
        recycleCredit.setLayoutManager(layoutManager_credit);
        recycleCredit.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager_crypto = new LinearLayoutManager(view.getContext());
        layoutManager_crypto.setOrientation(LinearLayoutManager.VERTICAL);
        recycleCrypto.setLayoutManager(layoutManager_crypto);
        recycleCrypto.setItemAnimator(new DefaultItemAnimator());

        balanceCreditAdaptor = new BalanceCreditAdaptor(getActivity(), moneyList);
        balanceCryptoAdaptor = new BalanceCryptoAdaptor(getActivity(), cryptoList);
        recycleCredit.setAdapter(balanceCreditAdaptor);
        recycleCrypto.setAdapter(balanceCryptoAdaptor);

        lySwipeRefresh.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.mainGold));

        lblCurrencyUnit.setText(getString(R.string.turk_lirasi));

        currencyTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                progressLoadingCrypto.setVisibility(View.VISIBLE);
                switch (tab.getPosition()) {
                    case 0:
//                        lblCurrencyUnit.setText(getString(R.string.turk_lirasi)); // Activated if networth has other units
                        updateBalanceValues(Currencies.TRY);
                        break;
                    case 1:
//                        lblCurrencyUnit.setText(getString(R.string.dollor)); // Activated if networth has other units
                        updateBalanceValues(Currencies.USD);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        lySwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendRequestToAPI();
            }
        });

//        for (int i = 0; i < rvAttendanceItems.getChildCount(); i++) {
//            AttendanceViewHolder holder = (AttendanceViewHolder) rv.findViewHolderForAdapterPosition(i);
//            holder.sStatus.getSelectedItemId();
//        }

        lblHideCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recycleCredit.getVisibility() == View.VISIBLE) {
                    lblHideCredit.setText(getResources().getString(R.string.show));
                    recycleCredit.setVisibility(View.GONE);
                } else {
                    recycleCredit.setVisibility(View.VISIBLE);
                    lblHideCredit.setText(getResources().getString(R.string.hide));
                }
//                if (balanceHidden) {
//                    for (int index = 0; index < recycleCredit.getChildCount(); index++) {
//                        RelativeLayout relativeLayout = recycleCredit.findViewHolderForAdapterPosition(index).itemView.findViewById(R.id.lyBalanceSection);
//                        relativeLayout.animate().alpha(1.0f);
//                        relativeLayout.setVisibility(View.VISIBLE);
//                    }
//                    balanceHidden = false;
//                    lblHideCredit.setText(getResources().getString(R.string.hide));
//                } else {
//                    for (int index = 0; index < recycleCredit.getChildCount(); index++) {
//                        RelativeLayout relativeLayout = recycleCredit.findViewHolderForAdapterPosition(index).itemView.findViewById(R.id.lyBalanceSection);
//                        relativeLayout.animate().alpha(0.0f);
//                        relativeLayout.setVisibility(View.GONE);
//                    }
//                    balanceHidden = true;
//                    lblHideCredit.setText(getResources().getString(R.string.show));
//                }
            }
        });

        lblHideZeroCrypto.setOnClickListener( v-> {
            if(filterIsOn){
                filterIsOn = false;
                cryptoList.clear();
                cryptoList.addAll(cryptoList_backup);
                lblHideZeroCrypto.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btnlayout_gold_border_small));
                lblHideZeroCrypto.setText(getActivity().getResources().getString(R.string.hide_zeros));
            } else {
                filterIsOn = true;
                filterZeroValues();
                lblHideZeroCrypto.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btnlayout_green));
                lblHideZeroCrypto.setText(getActivity().getResources().getString(R.string.show_all));
            }
            balanceCryptoAdaptor.notifyDataSetChanged();
        });


//        switchHideZeroCrypto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
//                if(check){
//                    filterIsOn = true;
//                    filterZeroValues();
//                } else {
//                    filterIsOn = false;
//                    cryptoList.clear();
//                    cryptoList.addAll(cryptoList_backup);
//                }
//                balanceCryptoAdaptor.notifyDataSetChanged();
//            }
//        });

        lblDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent depositIntent = new Intent(view.getContext(), DepositActivity.class);
                startActivity(depositIntent);
            }
        });

        lblWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent depositIntent = new Intent(view.getContext(), WithdrawActivity.class);
                startActivity(depositIntent);
            }
        });

        marketViewModel.setShowWaitingBar(true);
        sendRequestToAPI();
        listenForComingDataFromServer();
    }

    private void sendRequestToAPI() {
        final String url = new RequestUrls().getUrl(RequestType.WALLET_BALANCE);
        final RequestBody requestBody = RequestBody.create(new byte[0]);
        final Request request = new Request.Builder().url(url).addHeader("Authorization", utils.getDecryptedSharedPreferences(getActivity(), "token")).post(requestBody).tag(BALANCE_TAG).build();
        marketViewModel.setApiRequest(request, RequestType.WALLET_BALANCE);
    }

    private void listenForComingDataFromServer() {
        marketViewModel.getApiResponse().observe(getViewLifecycleOwner(), responsePair -> {
            String response = responsePair.first;
            RequestType requestType = responsePair.second;

            switch (requestType) {
                case WALLET_BALANCE:
                    try {
                        parseResponse(response, Currencies.TRY);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            marketViewModel.setShowWaitingBar(false);
        });

        marketViewModel.isRequestFailed().observe(getViewLifecycleOwner(), failurePair -> {
            RequestType requestType = failurePair.second;

            if(requestType == RequestType.WALLET_BALANCE) {
                marketViewModel.setShowWaitingBar(false);
                Toast.makeText(getContext(), getActivity().getResources().getString(R.string.failed_to_refresh_balance), Toast.LENGTH_SHORT).show();
                if (lySwipeRefresh.isRefreshing()) { lySwipeRefresh.setRefreshing(false); }
            }
        });
    }

    private void parseResponse(String response, Currencies currency) throws JSONException {
        JSONObject jsonBalance = new JSONObject(response).getJSONObject("balances");
        parseNetWorth(jsonBalance.getString("totalBalance"));
        parseCredit(jsonBalance.getJSONObject("fiat_balances"), currency); // money unit is not useful now, if new units e.g. GBP and EUR added it can be used!
        parseCrypto(jsonBalance.getJSONObject("balances"), currency);
        updateUI();
    }

    private void parseNetWorth(String net_worth) {
        lblBalance.setText(utils.reduceDecimal(net_worth,2));
    }

    private void parseCredit(JSONObject credits_balances, Currencies currency) throws JSONException {
        credit_json_object = credits_balances; // saves credit data
        moneyList.clear();
        Iterator<String> credits = credits_balances.keys();
        while (credits.hasNext()) {
            String key = credits.next();
            JSONObject entry = credits_balances.getJSONObject(key);
            moneyList.add(new BalanceCreditObject(
                    entry.getString("name"), // currency name
                    entry.getString("TRY"), // "TRY" equivalent --- if other credit units e.g. EUR or GBP added then "TRY" should be replaced by entry.getString(key)
                    "0",
                    null, // amount of key currency value
                    entry.getString(currency.toString()), // equivalent currency value
                    currency.toString() // equivalent currency name
            ));
        }
    }
    private void parseCrypto(JSONObject crypto_balances, Currencies currency) throws JSONException {
        crypto_json_object = crypto_balances; // saves crypto data
        cryptoList_backup.clear();
        Iterator<String> cryptos = crypto_balances.keys();
        while (cryptos.hasNext()) {
            String key = cryptos.next();
            JSONObject entry = crypto_balances.getJSONObject(key);
            cryptoList_backup.add(new BalanceCryptoObject(
                    entry.getString("asset_name"),
                    entry.getString("available"),
                    entry.getString("freeze"),
                    entry.getString("total"),
                    entry.getString(currency.toString()),
                    currency.toString().toLowerCase()
            ));
        }
    }

    private void updateBalanceValues(Currencies currency) {
        try {
            parseCredit(credit_json_object, currency);
            parseCrypto(crypto_json_object, currency);
            updateUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        progressLoadingCredit.setVisibility(View.GONE);
        progressLoadingCrypto.setVisibility(View.GONE);
        recycleCredit.setVisibility(View.VISIBLE);
        recycleCrypto.setVisibility(View.VISIBLE);

        // if list was in the refreshing mode it will set to false
        lySwipeRefresh.setRefreshing(false);

        // update credit list
        balanceCreditAdaptor.notifyDataSetChanged();

        // update crypto list
        cryptoList.clear();
        cryptoList.addAll(cryptoList_backup);
        if(filterIsOn) { filterZeroValues(); }
        balanceCryptoAdaptor.notifyDataSetChanged();
    }

    private void filterZeroValues() {
        cryptoList.clear();
        for (BalanceCryptoObject crypto: cryptoList_backup) {
            if (Double.valueOf(crypto.getTotal()) != 0) {
                cryptoList.add(crypto);
            }
        }
    }
//
//    private void failureResponse (int http_code) {
//        // Create an alert builder
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//
//        // set the custom layout
//        final View dialogView = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
//        builder.setView(dialogView);
//
//        AlertDialog dialog = builder.create(); // create alert dialog
//        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
//        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
//        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
//        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
//
//        btnNegative.setVisibility(View.GONE); // No need for this button here!
//        btnPositive.setText(getString(R.string.ok));
//
//        if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED){
//            lblMsgHeader.setText(getResources().getString(R.string.sessionTimeOut));
//            lblMsg.setText(R.string.session_timeout_msg);
//        } else {
//            lblMsgHeader.setText(getResources().getString(R.string.error));
//            lblMsg.setText(String.valueOf(http_code));
//        }
//
//        btnPositive.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                if (http_code == HttpsURLConnection.HTTP_UNAUTHORIZED) {
//                    Intent loginActivity = new Intent(getActivity(), MainRegistrationActivity.class);
//                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(loginActivity);
//                    getActivity().finishAffinity();
//                }
//            }
//        });
//        // show dialog
//        dialog.show();
//    }

//    private void failureRequest(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//
//        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
//        builder.setView(dialogView);
//
//        AlertDialog dialog = builder.create(); // create alert dialog
//        TextView lblMsgHeader = (TextView) dialogView.findViewById(R.id.lblMsgHeader);
//        TextView lblMsg = (TextView) dialogView.findViewById(R.id.lblMsg);
//        Button btnNegative = (Button) dialogView.findViewById(R.id.btnNegative);
//        Button btnPositive = (Button) dialogView.findViewById(R.id.btnPositive);
//        btnNegative.setVisibility(View.GONE); // No need for this button here!
//        lblMsgHeader.setText(getActivity().getResources().getString(R.string.error));
//        lblMsg.setText(getActivity().getResources().getString(R.string.server_connection_failure));
//        btnPositive.setText(getActivity().getResources().getString(R.string.try_again));
//        dialog.setCancelable(true);
//
//        btnPositive.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                sendRequestToAPI();
//            }
//        });
//        dialog.show();
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}