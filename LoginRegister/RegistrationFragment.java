package com.arsinex.com.LoginRegister;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationFragment extends Fragment {

    private static final int INVITATION_CODE_LENGTH = 8;

    private ImageView btnBack;

    private TabLayout tabRegisterType;
    private RelativeLayout lyPhoneSection, lyEmailSection;

    private TextInputLayout lyTxtName, lyTxtSurname, lyTxtEmail, lyTxtPhone,  lyTxtPasswordVerify, lyTxtPassword, lyTxtInvitationCode;
    private TextInputEditText txtName, txtSurname, txtEmail, txtPhone, txtPassword, txtPasswordVerify, txtInvitationCode;
    private TextView lblTermsConditions;
    private Button btnRegister;
    private CheckBox chkTermsConditions;

    private Boolean agreedToTerms = false;

    private Utils utils = new Utils();

    private LoginRegisterViewModel loginRegisterViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View registerView = inflater.inflate(R.layout.fragment_registration, container, false);

        loginRegisterViewModel = new ViewModelProvider(requireActivity()).get(LoginRegisterViewModel.class);

        return registerView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabRegisterType = (TabLayout) view.findViewById(R.id.tabRegisterType);

        btnBack = (ImageView) view.findViewById(R.id.btnBack);

        lyEmailSection = (RelativeLayout) view.findViewById(R.id.lyEmailSection);
        lyPhoneSection = (RelativeLayout) view.findViewById(R.id.lyPhoneSection);

        lyTxtName = (TextInputLayout) view.findViewById(R.id.lyTxtName);
        txtName = (TextInputEditText) view.findViewById(R.id.txtName);
        lyTxtSurname = (TextInputLayout) view.findViewById(R.id.lyTxtSurname);
        txtSurname = (TextInputEditText) view.findViewById(R.id.txtSurname);
        lyTxtEmail = (TextInputLayout) view.findViewById(R.id.lyTxtEmail);
        txtEmail = (TextInputEditText) view.findViewById(R.id.txtEmail);
        lyTxtPhone = (TextInputLayout) view.findViewById(R.id.lyTxtPhone);
        txtPhone = (TextInputEditText) view.findViewById(R.id.txtPhone);
        lyTxtPassword = (TextInputLayout) view.findViewById(R.id.lyTxtPassword);
        txtPassword = (TextInputEditText) view.findViewById(R.id.txtPassword);
        lyTxtPasswordVerify = (TextInputLayout) view.findViewById(R.id.lyTxtPasswordVerify);
        txtPasswordVerify = (TextInputEditText) view.findViewById(R.id.txtPasswordVerify);
        lyTxtInvitationCode = (TextInputLayout) view.findViewById(R.id.lyTxtInvitationCode);
        txtInvitationCode = (TextInputEditText) view.findViewById(R.id.txtInvitationCode);
        lblTermsConditions = (TextView) view.findViewById(R.id.lblTermsConditions);
        chkTermsConditions = (CheckBox) view.findViewById(R.id.chkTermsConditions);
        btnRegister = (Button) view.findViewById(R.id.btnRegister);

        tabRegisterType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    lyPhoneSection.setVisibility(View.GONE);
                    lyEmailSection.setVisibility(View.VISIBLE);
                } else {
                    lyPhoneSection.setVisibility(View.VISIBLE);
                    lyEmailSection.setVisibility(View.GONE);
                }
                // on mode change password field resets.
                txtPassword.setText("");
                txtPasswordVerify.setText("");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        txtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus){
                    lyTxtName.setErrorEnabled(false);
                }
            }
        });

        txtSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus){
                    lyTxtSurname.setErrorEnabled(false);
                }
            }
        });

        txtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus && !txtEmail.getText().toString().isEmpty() && !utils.isEmailValid(txtEmail.getText().toString())){
                    lyTxtEmail.setError(getResources().getString(R.string.not_valid_email));
                } else {
                    lyTxtEmail.setErrorEnabled(false);
                }
            }
        });

        txtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus && !txtPassword.getText().toString().isEmpty() && !utils.isValidPassword(txtPassword.getText().toString())){
                    lyTxtPassword.setError(getResources().getString(R.string.not_valid_password));
                } else {
                    lyTxtPassword.setErrorEnabled(false);
                }
            }
        });

        lyTxtPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtPassword.getTransformationMethod() != null && txtPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                    txtPassword.setTransformationMethod(null);
                    txtPasswordVerify.setTransformationMethod(null);
                    lyTxtPassword.setEndIconDrawable(R.drawable.ic_password_invisible_eye);
                } else {
                    txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    txtPasswordVerify.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    lyTxtPassword.setEndIconDrawable(R.drawable.ic_password_visible_eye);
                }
            }
        });

        txtPasswordVerify.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus && !txtPasswordVerify.getText().toString().isEmpty() && !txtPassword.getText().toString().equals(txtPasswordVerify.getText().toString())){
                    lyTxtPasswordVerify.setError(getResources().getString(R.string.not_match_passwords));
                } else {
                    lyTxtPasswordVerify.setErrorEnabled(false);
                }
            }
        });

        txtInvitationCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus && txtInvitationCode.getText().toString().length() < INVITATION_CODE_LENGTH) {
                    lyTxtInvitationCode.setError("Invitation code has 8 characters length.");
                } else {
                    lyTxtInvitationCode.setErrorEnabled(false);
                }
            }
        });

        // Terms and conditions span text
        SpannableString stringTermsConditions = new SpannableString(getResources().getString(R.string.terms_and_conditions));
        ClickableSpan termsAndConditionsSpanClick = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                generateTermsAndConditionsDialog();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getContext().getColor(R.color.text_color_1));
            }
        };

        stringTermsConditions.setSpan(termsAndConditionsSpanClick, 0, getResources().getString(R.string.terms_and_conditions).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTermsConditions.setClickable(true);
        lblTermsConditions.setText(stringTermsConditions);
        lblTermsConditions.setMovementMethod(LinkMovementMethod.getInstance());

        chkTermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agreedToTerms) {
                    agreedToTerms = false;
                } else {
                    generateTermsAndConditionsDialog();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tabRegisterType.getSelectedTabPosition() ==  0) {
                    registerWithEmail();
                } else {
                    registerWithPhone();
                }
            }
        });

        listenForAPIResponse();
    }

    private void listenForAPIResponse() {
        loginRegisterViewModel.isRequestFailed().observe(getViewLifecycleOwner(), requestFailed -> {
            if (requestFailed) { btnRegister.setClickable(true); }
        });
    }

    private void registerWithEmail() {
        if (formValidityCheck()) {
            btnRegister.setClickable(false);
            try {
                final JSONObject jsonRequest = new JSONObject()
                        .put("email", txtEmail.getText().toString())
                        .put("name", txtName.getText().toString())
                        .put("surname", txtSurname.getText().toString())
                        .put("password", txtPassword.getText().toString())
                        .put("password_confirm", txtPassword.getText().toString())
                        .put("invite_code", txtInvitationCode.getText().toString());
                sendAPIRequest(jsonRequest, RequestType.REGISTER_EMAIL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerWithPhone() {
        if (formValidityCheck()) {
            btnRegister.setClickable(false);
            try {
                final JSONObject jsonRequest = new JSONObject()
                        .put("phone", txtPhone.getText().toString())
                        .put("name", txtName.getText().toString())
                        .put("surname", txtSurname.getText().toString())
                        .put("password", txtPassword.getText().toString())
                        .put("password_confirm", txtPasswordVerify.getText().toString())
                        .put("invite_code", txtInvitationCode.getText().toString());
                sendAPIRequest(jsonRequest, RequestType.REGISTER_PHONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAPIRequest(JSONObject jsonRequest, RequestType requestType){
        loginRegisterViewModel.setShowWaitingBar(true);
        loginRegisterViewModel.setJsonRequest(jsonRequest, requestType);
    }

    private boolean formValidityCheck(){

        if (txtName.getText().toString().isEmpty()) {
            lyTxtName.setError(getResources().getString(R.string.name_surname_required));
            return false;
        }

        if (txtSurname.getText().toString().isEmpty()) {
            lyTxtSurname.setError(getResources().getString(R.string.name_surname_required));
            return false;
        }

        // checks email field if login is with email
        if (tabRegisterType.getSelectedTabPosition() == 0) {
            if (txtEmail.getText().toString().isEmpty() || !utils.isEmailValid(txtEmail.getText().toString())) {
                lyTxtEmail.setError(getResources().getString(R.string.not_valid_email));
                return false;
            }
        }

        // checks phone number if login is with phone number
        if (tabRegisterType.getSelectedTabPosition() == 1) {
            if (txtPhone.getText().toString().length() < 5) {
                Toast.makeText(this.getContext(), "Phone number is not valid.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if(txtPassword.getText().toString().isEmpty() || !utils.isValidPassword(txtPassword.getText().toString())) {
            lyTxtPassword.setError(getResources().getString(R.string.not_valid_password));
            return false;
        }
        if (txtPasswordVerify.getText().toString().isEmpty() || !txtPassword.getText().toString().equals(txtPasswordVerify.getText().toString())) {
            lyTxtPasswordVerify.setError(getResources().getString(R.string.not_match_passwords));
            return false;
        }
        if (!chkTermsConditions.isChecked()) {
            Toast termsAndConditionsToast = Toast.makeText(getContext(), R.string.terms_and_conditions_check, Toast.LENGTH_SHORT);
            TextView v = (TextView) termsAndConditionsToast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            termsAndConditionsToast.show();
            return false;
        }
        return true;
    }

    private void generateTermsAndConditionsDialog() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String LANG_CURRENT = preferences.getString("language", "tr");

        Dialog termsAndConditionsDialog = new Dialog(getContext());
        termsAndConditionsDialog.setContentView(R.layout.terms_and_conditions);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.95);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.9);
        termsAndConditionsDialog.getWindow().setLayout(width, height);
        termsAndConditionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WebView wvTerms = (WebView) termsAndConditionsDialog.findViewById(R.id.wvTerms);
        TextView btnAgree = termsAndConditionsDialog.findViewById(R.id.btnAgree);
        wvTerms.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.main_BG_dark));

        wvTerms.loadUrl("file:///android_asset/TermsAndConditions-" + LANG_CURRENT);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chkTermsConditions.isChecked()) { chkTermsConditions.setChecked(true); }
                agreedToTerms = true;
                termsAndConditionsDialog.dismiss();
            }
        });
        termsAndConditionsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                chkTermsConditions.setChecked(false);
            }
        });
        termsAndConditionsDialog.show();
    }

    @Override
    public void onPause() {
        txtPassword.setText("");
        txtPasswordVerify.setText("");
        super.onPause();
    }
}