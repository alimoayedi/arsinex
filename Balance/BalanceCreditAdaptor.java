package com.arsinex.com.Balance;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.arsinex.com.FetchAndLoadImage;
import com.arsinex.com.Objects.BalanceCreditObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class BalanceCreditAdaptor extends RecyclerView.Adapter<BalanceCreditAdaptor.MyViewHolder> {

    private ArrayList<BalanceCreditObject> creditList;
    private Activity activity;

    private Utils utils = new Utils();

    public BalanceCreditAdaptor(Activity activity, ArrayList<BalanceCreditObject> creditList) {
        this.creditList = creditList;
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView lblMoneyName, lblBalanceAmount, lblSymbol, lblFreeze, lblBalanceEqual, lblBalanceEqualSymbol;
        private ImageView imgLogo;
        private RelativeLayout lyBalanceSection, lyEquivalentSection, lyFreeze;


        MyViewHolder(View view) {
            super(view);
            imgLogo = view.findViewById(R.id.imgLogo);
            lblMoneyName = view.findViewById(R.id.lblMoneyName);
            lyBalanceSection = view.findViewById(R.id.lyBalanceSection);
            lblBalanceAmount = view.findViewById(R.id.lblBalanceAmount);
            lblSymbol = view.findViewById(R.id.lblSymbol);
            lyEquivalentSection = view.findViewById(R.id.lyEquivalentSection);
            lblBalanceEqual = view.findViewById(R.id.lblBalanceEqual);
            lblBalanceEqualSymbol = view.findViewById(R.id.lblBalanceEqualSymbol);
            lyFreeze = view.findViewById(R.id.lyFreeze);
            lblFreeze = view.findViewById(R.id.lblFreeze);

        }
    }

    @NonNull
    @NotNull
    @Override
    public BalanceCreditAdaptor.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_new_currency_balance, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BalanceCreditAdaptor.MyViewHolder holder, int position) {
        BalanceCreditObject moneyObject = creditList.get(position);
        holder.lblMoneyName.setText(moneyObject.getAsset_name());
        holder.lblBalanceAmount.setText(utils.reduceDecimal(moneyObject.getAvailable(),2));

        // set equivalent label and value
        if (moneyObject.getAsset_name().toLowerCase().equals(moneyObject.getEquivalent_unit().toLowerCase())) {
            holder.lyEquivalentSection.setVisibility(View.INVISIBLE);
        } else {
            holder.lyEquivalentSection.setVisibility(View.VISIBLE);
            holder.lblBalanceEqual.setText(utils.reduceDecimal(moneyObject.getEquivalent(), 2));
        }

        // set freeze value and label
        String inProcess = moneyObject.getFreeze();
        if (inProcess.equals("0")){
            holder.lyFreeze.setVisibility(View.GONE);
        } else {
            holder.lyFreeze.setVisibility(View.VISIBLE);
            holder.lblFreeze.setText(utils.reduceDecimal(inProcess, 2));
        }

        // set label
        switch (moneyObject.getAsset_name().toLowerCase()) {
            case "try":
                holder.lblSymbol.setText(R.string.lira_symbol);
                break;
            case "usd":
                holder.lblSymbol.setText(R.string.dollar_symbol);
                break;
            case "eur":
                holder.lblSymbol.setText(R.string.euro_symbol);
                break;
            case "gbp":
                holder.lblSymbol.setText(R.string.sterling_symbol);
                break;
        }

        // set equivalent label
        switch (moneyObject.getEquivalent_unit().toLowerCase()) {
            case "try":
                holder.lblBalanceEqualSymbol.setText(R.string.lira_symbol);
                break;
            case "usd":
                holder.lblBalanceEqualSymbol.setText(R.string.dollar_symbol);
                break;
            case "eur":
                holder.lblBalanceEqualSymbol.setText(R.string.euro_symbol);
                break;
            case "gbp":
                holder.lblBalanceEqualSymbol.setText(R.string.sterling_symbol);
                break;
        }
        FetchAndLoadImage fetchAndLoadImage = new FetchAndLoadImage(activity.getApplicationContext());
        fetchAndLoadImage.setImage(null, holder.imgLogo, "ic_" + moneyObject.getAsset_name());
    }

    @Override
    public int getItemCount() {
        return creditList.size();
    }

//    private void openPopupWindow(View viewAnchor, BalanceCreditObject moneyObject) {
//        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(activity.getBaseContext().LAYOUT_INFLATER_SERVICE);
//        View popupView = layoutInflater.inflate(R.layout.layout_balance_popup, null);
//
//        TextView lblBalanceAmount = popupView.findViewById(R.id.lblBalanceAmount);
//        TextView lblSymbol = popupView.findViewById(R.id.lblSymbol);
//        RelativeLayout lyEquivalentSection = popupView.findViewById(R.id.lyEquivalentSection);
//        TextView lblBalanceEqual = popupView.findViewById(R.id.lblBalanceEqual);
//        TextView lblBalanceEqualSymbol = popupView.findViewById(R.id.lblBalanceEqualSymbol);
//        TextView lblFreeze = popupView.findViewById(R.id.lblFreeze);
//
//        lblBalanceAmount.setText(moneyObject.getAvailable());
//        // set label
//        switch (moneyObject.getAsset_name().toLowerCase()) {
//            case "try":
//                lblSymbol.setText(R.string.lira_symbol);
//                break;
//            case "usd":
//                lblSymbol.setText(R.string.dollar_symbol);
//                break;
//            case "eur":
//                lblSymbol.setText(R.string.euro_symbol);
//                break;
//            case "gbp":
//                lblSymbol.setText(R.string.sterling_symbol);
//                break;
//        }
//
//        // set equivalent label and value
//        if (moneyObject.getAsset_name().toLowerCase().equals(moneyObject.getEquivalent_unit().toLowerCase())) {
//            lyEquivalentSection.setVisibility(View.GONE);
//        } else {
//            lyEquivalentSection.setVisibility(View.VISIBLE);
//            lblBalanceEqual.setText(utils.reduceDecimal(moneyObject.getEquivalent(), 2));
//        }
//
//        // set equivalent label
//        switch (moneyObject.getEquivalent_unit().toLowerCase()) {
//            case "try":
//                lblBalanceEqualSymbol.setText(R.string.lira_symbol);
//                break;
//            case "usd":
//                lblBalanceEqualSymbol.setText(R.string.dollar_symbol);
//                break;
//            case "eur":
//                lblBalanceEqualSymbol.setText(R.string.euro_symbol);
//                break;
//            case "gbp":
//                lblBalanceEqualSymbol.setText(R.string.sterling_symbol);
//                break;
//        }
//
//        lblFreeze.setText(moneyObject.getFreeze());
//
//        PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
//        popupWindow.showAsDropDown(viewAnchor, 0, -2*(viewAnchor.getHeight()));
//
//    }
}
