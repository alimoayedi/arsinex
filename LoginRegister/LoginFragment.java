package com.arsinex.com.LoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.PasswordRecoverActivity;
import com.arsinex.com.R;
import com.arsinex.com.SettingActivity;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.LoginRegistrationFragments;
import com.arsinex.com.enums.RequestType;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {

    private TabLayout tabLoginType;
    private RelativeLayout lyPhoneSection, lyEmailSection;
    private TextView lblForgetPass;
    private TextInputLayout lyTxtEmail, lyTxtPhone, lyTxtPassword;
    private TextInputEditText txtEmail, txtPhone, txtPassword;
    private CheckBox chkRememberMe;
    private Button btnLogin, btnRegister;
    private ImageView btnSetting;

    private Utils utils = new Utils();

    private LoginRegisterViewModel loginRegisterViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View registerView = inflater.inflate(R.layout.fragment_login, container, false);

        loginRegisterViewModel = new ViewModelProvider(requireActivity()).get(LoginRegisterViewModel.class);

        return registerView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSetting = (ImageView) view.findViewById(R.id.btnSetting);
        tabLoginType = (TabLayout) view.findViewById(R.id.tabLoginType);
        lyEmailSection = (RelativeLayout) view.findViewById(R.id.lyEmailSection);
        lyPhoneSection = (RelativeLayout) view.findViewById(R.id.lyPhoneSection);
        lblForgetPass = (TextView) view.findViewById(R.id.lblForgetPass);
        lyTxtEmail = (TextInputLayout) view.findViewById(R.id.lyTxtEmail);
        lyTxtPhone = (TextInputLayout) view.findViewById(R.id.lyTxtPhone);
        lyTxtPassword = (TextInputLayout) view.findViewById(R.id.lyTxtPassword);
        txtEmail = (TextInputEditText) view.findViewById(R.id.txtEmail);
        txtPhone = (TextInputEditText) view.findViewById(R.id.txtPhone);
        txtPassword = (TextInputEditText) view.findViewById(R.id.txtPassword);
        chkRememberMe = (CheckBox) view.findViewById(R.id.chkRememberMe);
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnRegister = (Button) view.findViewById(R.id.btnRegister);

        btnSetting.setOnClickListener( clickableView -> {
            Intent settingActivity = new Intent(getContext(), SettingActivity.class);
            getActivity().startActivity(settingActivity);
            getActivity().finish();
        });

        setRememberMeCheckBox();

        tabLoginType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        txtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(focus) { lyTxtEmail.setErrorEnabled(false); }
            }
        });

        txtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {lyTxtPhone.setErrorEnabled(false);}
            }
        });

        txtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) { lyTxtPassword.setErrorEnabled(false); }
            }
        });

        lyTxtPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtPassword.getTransformationMethod() != null && txtPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                    txtPassword.setTransformationMethod(null);
                    lyTxtPassword.setEndIconDrawable(R.drawable.ic_password_invisible_eye);
                } else {
                    txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    lyTxtPassword.setEndIconDrawable(R.drawable.ic_password_visible_eye);
                }
            }
        });

        chkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (tabLoginType.getSelectedTabPosition() == 0) {
                    if (checked) {
                        utils.saveInSharedPreferences(getActivity(), "email", txtEmail.getText().toString());
                    } else {
                        utils.removeFromSharedPreferences(getActivity(), "email");
                    }
                } else {
                    if (checked) {
                        utils.saveInSharedPreferences(getActivity(), "phone", txtEmail.getText().toString());
                    } else {
                        utils.removeFromSharedPreferences(getActivity(), "phone");
                    }
                }
            }
        });

        lblForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recoverPasswordActivity = new Intent(LoginFragment.this.getContext(), PasswordRecoverActivity.class);
                startActivity(recoverPasswordActivity);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLoginType.getSelectedTabPosition() == 0) {
                    loginWithEmail();
                } else {
                    loginWithPhone();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginRegisterViewModel.setFragment(LoginRegistrationFragments.REGISTER);
            }
        });

        listenForAPIResponse();
    }

    private void listenForAPIResponse() {
        loginRegisterViewModel.isRequestFailed().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                btnLogin.setClickable(true);
            }
        });
    }

    private void loginWithEmail() {
        if (checkFormValidity()) {
            btnLogin.setClickable(false);
            try {
                final JSONObject jsonRequest = new JSONObject()
                        .put("email", txtEmail.getText().toString())
                        .put("password", txtPassword.getText().toString());
                sendAPIRequest(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void loginWithPhone() {
        if (checkFormValidity()) {
            btnLogin.setClickable(false);
            try {
                final JSONObject jsonRequest = new JSONObject()
                        .put("phone", txtPhone.getText().toString())
                        .put("password", txtPassword.getText().toString());
                sendAPIRequest(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAPIRequest(JSONObject jsonRequest) {
        loginRegisterViewModel.setShowWaitingBar(true);
        loginRegisterViewModel.setJsonRequest(jsonRequest, RequestType.LOGIN);
    }

    private boolean checkFormValidity(){

        // checks email field if login is with email
        if (tabLoginType.getSelectedTabPosition() == 0) {
            if (txtEmail.getText().toString().isEmpty() || !utils.isEmailValid(txtEmail.getText().toString())) {
                lyTxtEmail.setError(getResources().getString(R.string.not_valid_email));
                return false;
            }
        }

        // checks phone number if login is with phone number
        if (tabLoginType.getSelectedTabPosition() == 1) {
            if (txtPhone.getText().toString().isEmpty() || txtPhone.getText().toString().length() < 5) {
                lyTxtPhone.setError(getResources().getString(R.string.not_valid_phone));
                return false;
            }
        }

        // checks password
        if (txtPassword.getText().toString().isEmpty() || txtPassword.getText().toString().length() < 8) {
            lyTxtPassword.setError(getResources().getString(R.string.short_password));
            return false;
        }

        return true;
    }

    private void setRememberMeCheckBox() {
        if (utils.getFromSharedPreferences(this.getActivity(), "email") != null) {
            txtEmail.setText(utils.getFromSharedPreferences(this.getActivity(),"email"));
            chkRememberMe.setChecked(true);
        }
    }

    @Override
    public void onPause() {
        txtPassword.setText("");
        super.onPause();
    }
}

