package com.arsinex.com.LoginRegister;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class AddEmailFragment extends Fragment {

    private TextInputLayout lyTxtEmail;
    private TextInputEditText txtEmail;
    private Button btnSendEmail;
    private ImageView btnBack;

    private Utils utils = new Utils();

    private LoginRegisterViewModel loginRegisterViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View addEmailView = inflater.inflate(R.layout.fragment_add_email, container, false);

        loginRegisterViewModel = new ViewModelProvider(requireActivity()).get(LoginRegisterViewModel.class);

        return addEmailView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        lyTxtEmail = (TextInputLayout) view.findViewById(R.id.lyTxtEmail);
        txtEmail = (TextInputEditText) view.findViewById(R.id.txtEmail);
        btnSendEmail = (Button) view.findViewById(R.id.btnSendEmail);
        btnBack = (ImageView) view.findViewById(R.id.btnBack);

        txtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    lyTxtEmail.setErrorEnabled(false);
                }
            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtEmail.getText().toString().isEmpty() || !utils.isEmailValid(txtEmail.getText().toString())){
                    lyTxtEmail.setError(getResources().getString(R.string.not_valid_email));
                } else {
                    btnSendEmail.setEnabled(false);
                    try {
                        sendAPIRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnBack.setOnClickListener( clickableView -> {
            getActivity().onBackPressed();
        });

        listenForAPIResponse();
    }

    private void listenForAPIResponse() {
        loginRegisterViewModel.isRequestFailed().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                btnSendEmail.setClickable(true);
            }
        });
    }

    private void sendAPIRequest() throws JSONException {
        final JSONObject jsonRequest = new JSONObject().put("mail", txtEmail.getText().toString());

        loginRegisterViewModel.setShowWaitingBar(true);
        loginRegisterViewModel.setJsonRequest(jsonRequest, RequestType.ADD_EMAIL);
    }

}