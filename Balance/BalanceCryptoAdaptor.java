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
import com.arsinex.com.Objects.BalanceCryptoObject;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class BalanceCryptoAdaptor extends RecyclerView.Adapter<BalanceCryptoAdaptor.MyViewHolder> {

    private static final String TRY = "try";
    private static final String USD = "usd";
    private static final String GBP = "gbp";
    private static final String EUR = "eur";

    private ArrayList<BalanceCryptoObject> cryptoList;
    private Activity activity;

    private Utils utils = new Utils();

    public BalanceCryptoAdaptor(Activity activity, ArrayList<BalanceCryptoObject> cryptoList) {
        this.cryptoList = cryptoList;
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView lblCryptoName, lblQuantity, lblEquivalent, lblEquivalentSymbol, lblFreeze, lblInProcessSymbol;
        private ImageView imgLogo;
        private RelativeLayout lyFreeze;
        MyViewHolder(View view) {
            super(view);
            lblCryptoName = view.findViewById(R.id.lblCryptoName);
            lblQuantity = view.findViewById(R.id.lblQuantity);
            lblEquivalent = view.findViewById(R.id.lblEquivalent);
            lblEquivalentSymbol = view.findViewById(R.id.lblEquivalentSymbol);
            lyFreeze = view.findViewById(R.id.lyFreeze);
            lblFreeze = view.findViewById(R.id.lblFreeze);
            lblInProcessSymbol = view.findViewById(R.id.lblInProcessSymbol);
            imgLogo = view.findViewById(R.id.imgLogo);
        }
    }

    @NonNull
    @NotNull
    @Override
    public BalanceCryptoAdaptor.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_crypto_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BalanceCryptoAdaptor.MyViewHolder holder, int position) {
        BalanceCryptoObject balanceCryptoObject = cryptoList.get(position);
        holder.lblCryptoName.setText(balanceCryptoObject.getAsset());
        holder.lblQuantity.setText(utils.reduceDecimal(balanceCryptoObject.getAvailable(),8));
        holder.lblEquivalent.setText(utils.reduceDecimal(balanceCryptoObject.getEquivalent(),2));
        switch (balanceCryptoObject.getEquivalent_unit().toLowerCase()) {
            case TRY:
                holder.lblEquivalentSymbol.setText("₺");
                break;
            case USD:
                holder.lblEquivalentSymbol.setText("$");
                break;
            case EUR:
                holder.lblEquivalentSymbol.setText("€");
                break;
            case GBP:
                holder.lblEquivalentSymbol.setText("£");
                break;
        }
        String inProcess = balanceCryptoObject.getFreeze();
        if (inProcess.equals("0")){
            holder.lyFreeze.setVisibility(View.INVISIBLE);
        } else {
            holder.lyFreeze.setVisibility(View.VISIBLE);
            holder.lblFreeze.setText(utils.reduceDecimal(inProcess, 2));
            holder.lblInProcessSymbol.setText(balanceCryptoObject.getAsset());
        }
        FetchAndLoadImage fetchAndLoadImage = new FetchAndLoadImage(activity.getApplicationContext());
        fetchAndLoadImage.setImage(null, holder.imgLogo, "ic_" + balanceCryptoObject.getLogoURL());
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }
}
