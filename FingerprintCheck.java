package com.arsinex.com;

import android.app.Activity;

import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;

public class FingerprintCheck {

    private Activity activity;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public FingerprintCheck (Activity activity) {
        this.activity = activity;
    }


}
