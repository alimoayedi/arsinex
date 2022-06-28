package com.arsinex.com.LoginRegister;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddPhoneFragment extends Fragment {

    private TextView lblHeader, lblExplaination, lblCountryCode;
    private TextInputLayout lyTxtPhone;
    private TextInputEditText txtPhone;
    private Button btnSendMessage;
    private ImageView btnBack;

    private LoginRegisterViewModel loginRegisterViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View addPhoneView = inflater.inflate(R.layout.fragment_add_phone, container, false);

        loginRegisterViewModel = new ViewModelProvider(requireActivity()).get(LoginRegisterViewModel.class);

        return addPhoneView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        lblHeader = (TextView) view.findViewById(R.id.lblHeader);
        lblExplaination = (TextView) view.findViewById(R.id.lblExplaination);
        lblCountryCode = (TextView) view.findViewById(R.id.lblCountryCode);
        lyTxtPhone = (TextInputLayout) view.findViewById(R.id.lyTxtPhone);
        txtPhone = (TextInputEditText) view.findViewById(R.id.txtPhone);
        btnSendMessage = (Button) view.findViewById(R.id.btnSendMessage);
        btnBack = (ImageView) view.findViewById(R.id.btnBack);

        txtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                lyTxtPhone.setErrorEnabled(false);
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtPhone.getText().toString().length() < 5) {
                    lyTxtPhone.setError("Phone number is not valid.");
                } else {
                    btnSendMessage.setClickable(false);
                    confirmPhone();
                }
            }
        });

        btnBack.setOnClickListener( clickableView -> {
            getActivity().onBackPressed();
        });

        listenForAPIResponse();
    }

    private void confirmPhone() {
        String fullPhoneNumber = lblCountryCode.getText().toString() + txtPhone.getText().toString();
        String msg = "<b>" + fullPhoneNumber + "</b>";

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View exitDialog = getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        builder.setView(exitDialog);

        TextView lblMsgHeader = (TextView) exitDialog.findViewById(R.id.lblMsgHeader);
        TextView lblMsg = (TextView) exitDialog.findViewById(R.id.lblMsg);
        Button btnNegative = (Button) exitDialog.findViewById(R.id.btnNegative);
        Button btnPositive = (Button) exitDialog.findViewById(R.id.btnPositive);

        lblMsgHeader.setText(getResources().getString(R.string.phone_number_reconfirm));
        lblMsg.setText(getResources().getString(R.string.phone_confirmation_p1) + "\n" + HtmlCompat.fromHtml(msg,HtmlCompat.FROM_HTML_MODE_LEGACY) + "\n" + getResources().getString(R.string.phone_confirmation_p2));
        btnPositive.setText(getResources().getString(R.string.yes));
        btnNegative.setText(getResources().getString(R.string.no));
        AlertDialog dialog = builder.create();
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendMessage.setEnabled(false);
                try {
                    sendAPIRequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                btnSendMessage.setClickable(true);
            }
        });
        dialog.show();
    }

    private void listenForAPIResponse() {
        loginRegisterViewModel.isRequestFailed().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                btnSendMessage.setClickable(true);
            }
        });
    }

    private void sendAPIRequest() throws JSONException {
        final JSONObject jsonRequest = new JSONObject().put("telephone", txtPhone.getText().toString());

        loginRegisterViewModel.setShowWaitingBar(true);
        loginRegisterViewModel.setJsonRequest(jsonRequest, RequestType.ADD_PHONE);
    }

}
