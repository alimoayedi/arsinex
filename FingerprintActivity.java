package com.arsinex.com;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

import com.arsinex.com.marketPackage.MarketActivity;
import com.arsinex.com.LoginRegister.LoginRegisterActivity;
import com.arsinex.com.Utilities.Utils;

public class FingerprintActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private Button btnEnterPasscode;
    private ImageView imgFingerprint;

    private Dialog pleaseWaitDialog;

    private final Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        checkFingerprintAvailability();

        btnEnterPasscode = (Button) findViewById(R.id.btnEnterPasscode);
        imgFingerprint = (ImageView) findViewById(R.id.imgFingerprint);

        btnEnterPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLoginActivity();
            }
        });

        imgFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFingerprint();
            }
        });


    }

    private void checkFingerprintAvailability() {
        switch (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                checkFingerprint();
                break;
            default:
                goToLoginActivity();
                break;
        }
    }

    private void checkFingerprint() {
        executor = ContextCompat.getMainExecutor(getApplicationContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull @NotNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull @NotNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                goToMarketActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getResources().getString(R.string.identity_verification_title))
                .setSubtitle(getResources().getString(R.string.identity_verification_finger_print))
                .setNegativeButtonText(getResources().getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToMarketActivity() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                pleaseWaitDialog = new Dialog(FingerprintActivity.this);
//                utils.setupDialog(pleaseWaitDialog, FingerprintActivity.this);
//                pleaseWaitDialog.show();
//            }
//        });

        Intent marketActivity = new Intent(FingerprintActivity.this, MarketActivity.class);
        marketActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        marketActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(marketActivity);
        finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
//        pleaseWaitDialog.dismiss();
        super.onStop();
    }
}