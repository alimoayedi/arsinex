package com.arsinex.com.LoginRegister;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;
import com.arsinex.com.enums.RequestType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class VerificationFragment extends Fragment {

    private static final int FIELD_ONE = 1;
    private static final int FIELD_TWO = 2;
    private static final int FIELD_THREE = 3;
    private static final int FIELD_FOUR = 4;
    private static final int FIELD_FIVE = 5;
    private static final int FIELD_SIX = 6;
    private static final int RESET_ALL_FIELDS = 7;
    private static final int ALL_FIELDS_DEACTIVE = 8;

    private ProgressBar progressBar;
    private TextView lblTitle, lblRequestSource; //lblCounter
    private EditText txtDigit1, txtDigit2, txtDigit3, txtDigit4, txtDigit5, txtDigit6;
    private Button btnContinue, btnResend;
    private long totalTimeCountInMilliseconds;
    private ImageView btnBack;

    private LoginRegisterViewModel loginRegisterViewModel;

    private Utils utils = new Utils();

    public static VerificationFragment newInstance(RequestType requestType, String contactInfo) {
        // initialize fragment
        VerificationFragment verificationFragment = new VerificationFragment();
        // initialize bundle
        Bundle args = new Bundle();
        // set fragment parameters
        args.putSerializable("requestType", requestType);
        args.putString("contactInfo", contactInfo);
        verificationFragment.setArguments(args);

        return verificationFragment;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_verification, container, false);

        loginRegisterViewModel = new ViewModelProvider(requireActivity()).get(LoginRegisterViewModel.class);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        btnBack = (ImageView) view.findViewById(R.id.btnBack);
        lblTitle = (TextView) view.findViewById(R.id.lblTitle);
        lblRequestSource = (TextView) view.findViewById(R.id.lblRequestSource);
        btnContinue = (Button) view.findViewById(R.id.btnContinue);
        btnResend = (Button) view.findViewById(R.id.btnResend);
        progressBar = (ProgressBar) view.findViewById(R.id.processBar);
        txtDigit1 = (EditText) view.findViewById(R.id.txtDigit1);
        txtDigit2 = (EditText) view.findViewById(R.id.txtDigit2);
        txtDigit3 = (EditText) view.findViewById(R.id.txtDigit3);
        txtDigit4 = (EditText) view.findViewById(R.id.txtDigit4);
        txtDigit5 = (EditText) view.findViewById(R.id.txtDigit5);
        txtDigit6 = (EditText) view.findViewById(R.id.txtDigit6);

        btnResend.setEnabled(false);
        btnContinue.setEnabled(false);

        setTimer(120); // 2 minutes wait
        startCountDownBar();

        String contactInfo = getArguments().getString("contactInfo");
        RequestType requestType = (RequestType) getArguments().getSerializable("requestType");

        switch (requestType) {
            case REGISTER_EMAIL:
            case ADD_EMAIL:
                lblTitle.setText(getResources().getString(R.string.email_confirmation));
                lblRequestSource.setText(getResources().getString(R.string.email_verification_msg_part1) + contactInfo + ". " + getResources().getString(R.string.email_verification_msg_part2));
                break;
            case REGISTER_PHONE:
            case ADD_PHONE:
                lblTitle.setText(getResources().getString(R.string.phone_confirmation));
                lblRequestSource.setText(getResources().getString(R.string.phone_verification_msg_part1) + contactInfo + ". " + getResources().getString(R.string.phone_verification_msg_part2));
                break;
            default:
                throw new IllegalArgumentException("Invalid Key, verification fragment");
        }

        txtDigit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count == 0) {
                    updateFields(1); // 1 : first edit text field
                } else {
                    txtDigit2.setEnabled(true);
                    txtDigit2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDigit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    updateFields(2); // 2 : second edit text field
                } else {
                    txtDigit3.setEnabled(true);
                    txtDigit3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDigit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    updateFields(3); // 3 : third edit text field
                } else {
                    txtDigit4.setEnabled(true);
                    txtDigit4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDigit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    updateFields(4); // 4 : forth edit text field
                } else {
                    txtDigit5.setEnabled(true);
                    txtDigit5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDigit5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    updateFields(5); // 5 : fifth edit text field
                } else {
                    txtDigit6.setEnabled(true);
                    txtDigit6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDigit6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    updateFields(6); // 6 : sixth edit text field
                } else {
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (requestType) {
                    case REGISTER_EMAIL:
                        loginRegisterViewModel.setResendRequest(RequestType.RESEND_REGISTER_EMAIL);
                        break;
                    case REGISTER_PHONE:
                        loginRegisterViewModel.setResendRequest(RequestType.RESEND_REGISTER_PHONE);
                        break;
                    case ADD_EMAIL:
                        loginRegisterViewModel.setResendRequest(RequestType.RESEND_ADD_EMAIL);
                        break;
                    case ADD_PHONE:
                        loginRegisterViewModel.setResendRequest(RequestType.RESEND_ADD_PHONE);
                        break;
                }
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonRequest = new JSONObject();
                disableKeyboard();

                String verificationCode = txtDigit1.getText().toString()
                        + txtDigit2.getText().toString()
                        + txtDigit3.getText().toString()
                        + txtDigit4.getText().toString()
                        + txtDigit5.getText().toString()
                        + txtDigit6.getText().toString();

                switch (requestType) {
                    case REGISTER_EMAIL:
                        try {
                            jsonRequest.put("code", verificationCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendAPIRequest(jsonRequest, RequestType.CODE_CONFIRMATION_EMAIL);
                        break;
                    case REGISTER_PHONE:
                        try {
                            jsonRequest.put("code", verificationCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendAPIRequest(jsonRequest, RequestType.CODE_CONFIRMATION_PHONE);
                        break;
                    case ADD_EMAIL:
                        try {
                            jsonRequest.put("mail", contactInfo);
                            jsonRequest.put("otp", verificationCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendAPIRequest(jsonRequest, RequestType.ADD_EMAIL_CONFIRM);
                        break;
                    case ADD_PHONE:
                        try {
                            jsonRequest.put("telephone", contactInfo);
                            jsonRequest.put("otp", verificationCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendAPIRequest(jsonRequest, RequestType.ADD_PHONE_CONFIRM);
                        break;
                    default:

                }
            }
        });

        btnBack.setOnClickListener( clickableView -> {
            getActivity().onBackPressed();
        });

        listenForAPIResponse();
    }

    private void listenForAPIResponse() {
        loginRegisterViewModel.getRequestResentStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                // activate timer, continue, and resend button
                startCountDownBar();
                updateFields(RESET_ALL_FIELDS);
                btnResend.setEnabled(false);
            }
        });
    }

    private void sendAPIRequest(JSONObject jsonRequest, RequestType requestType) {
        loginRegisterViewModel.setShowWaitingBar(true);
        loginRegisterViewModel.setJsonRequest(jsonRequest, requestType);
    }

    private void setTimer(int time) { // number of seconds
        totalTimeCountInMilliseconds = time * 1000;
        progressBar.setMax(time * 1000);
    }

    private void startCountDownBar() {
        new CountDownTimer(totalTimeCountInMilliseconds, 1) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                progressBar.setProgress((int) (leftTimeInMilliseconds));
            }

            @Override
            public void onFinish() {
                updateFields(ALL_FIELDS_DEACTIVE);
                btnResend.setEnabled(true);
                cancel();
            }
        }.start();
    }

    private void disableKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//    @Override
//    protected void onStart() {
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(networkChangeListener, filter);
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        unregisterReceiver(networkChangeListener);
//        super.onStop();
//    }

//    @Override
//    public void onBackPressed() {
//        AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
//        // exitDialog.setIcon();
//        exitDialog.setMessage(getResources().getString(R.string.exitQuestion));
//        exitDialog.setCancelable(false);
//        exitDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                Intent intent = new Intent(VerificationFragment.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//        exitDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog dialog = exitDialog.create();
//        dialog.show();
//    }

    private void updateFields(int activeField){
        switch (activeField){
            case FIELD_ONE:
                txtDigit2.setText("");
                txtDigit3.setText("");
                txtDigit4.setText("");
                txtDigit5.setText("");
                txtDigit6.setText("");
                txtDigit2.setEnabled(false);
                txtDigit3.setEnabled(false);
                txtDigit4.setEnabled(false);
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                break;
            case FIELD_TWO:
                txtDigit3.setText("");
                txtDigit4.setText("");
                txtDigit5.setText("");
                txtDigit6.setText("");
                txtDigit3.setEnabled(false);
                txtDigit4.setEnabled(false);
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                break;
            case FIELD_THREE:
                txtDigit4.setText("");
                txtDigit5.setText("");
                txtDigit6.setText("");
                txtDigit4.setEnabled(false);
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                break;
            case FIELD_FOUR:
                txtDigit5.setText("");
                txtDigit6.setText("");
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                break;
            case FIELD_FIVE:
                txtDigit6.setText("");
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                break;
            case FIELD_SIX:
                btnContinue.setEnabled(false);
                break;
            case RESET_ALL_FIELDS:
                txtDigit1.setText("");
                txtDigit2.setText("");
                txtDigit3.setText("");
                txtDigit4.setText("");
                txtDigit5.setText("");
                txtDigit6.setText("");
                txtDigit1.setEnabled(true);
                txtDigit1.requestFocus();
                txtDigit2.setEnabled(false);
                txtDigit3.setEnabled(false);
                txtDigit4.setEnabled(false);
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                btnResend.setEnabled(false);
                break;
            case ALL_FIELDS_DEACTIVE:
                txtDigit1.setEnabled(false);
                txtDigit2.setEnabled(false);
                txtDigit3.setEnabled(false);
                txtDigit4.setEnabled(false);
                txtDigit5.setEnabled(false);
                txtDigit6.setEnabled(false);
                btnContinue.setEnabled(false);
                btnResend.setEnabled(false);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        utils.removeEncryptedSharedPreferences(getActivity(),"token");
        super.onDestroyView();
    }
}
