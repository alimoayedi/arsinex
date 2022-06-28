package com.arsinex.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.arsinex.com.LoginRegister.LoginRegisterActivity;

public class SettingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private String currentLanguage;
    private TextView lblDayMode, lblNightMode, lblEnglish, lblTurkish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        super.onCreate(savedInstanceState);

        lblDayMode = (TextView) findViewById(R.id.lblDayMode);
        lblNightMode = (TextView) findViewById(R.id.lblNightMode);
        lblEnglish = (TextView) findViewById(R.id.lblEnglish);
        lblTurkish = (TextView) findViewById(R.id.lblTurkish);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        initializeDisplayModeButtons();
        initializeLanguageButtons();

        lblNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                // gets saved mode, if it doesn't exist will be set to false
                Boolean nightMode = sharedPreferences.getBoolean("night_mode",false);

                if (!nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveDisplayMode(true);
                    updateDisplayModeLabel(true);
                    recreate();
                }
            }
        });

        lblDayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                // gets saved mode, if it doesn't exist will be set to true
                Boolean nightMode = sharedPreferences.getBoolean("night_mode",true);

                if(nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveDisplayMode(false);
                    updateDisplayModeLabel(false);
                    recreate();
                }
            }
        });

        lblTurkish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                // for guarantee if the default value doesn't exists, value is set to "en" to update language into "tr"
                currentLanguage = preferences.getString("language", "en");

                // updates language if needed
                if (!currentLanguage.equals("tr")) {
                    saveLocale("tr");
                    finish();
                    startActivity(new Intent(SettingActivity.this, SettingActivity.class));
                }
            }
        });

        lblEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                // for guarantee if the default value doesn't exists, value is set to "tr" to update language into "en"
                currentLanguage = preferences.getString("language", "tr");

                // updates language if needed
                if (!currentLanguage.equals("en")) {
                    saveLocale("en");
                    finish();
                    startActivity(new Intent(SettingActivity.this, SettingActivity.class));
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    String activityName = getIntent().getExtras().getString("activity"); // gets the name of previous activity!
//                    Class<? extends Activity> targetActivity = Class.forName(activityName).asSubclass(Activity.class); // converts name of previous activity into class!
                    Intent launchIntent = new Intent(SettingActivity.this, LoginRegisterActivity.class);
                    finish();
                    startActivity(launchIntent);
//                } catch (ClassNotFoundException e ) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    private void initializeDisplayModeButtons() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean nightMode = sharedPreferences.getBoolean("night_mode",false);
        if (nightMode) {
            lblNightMode.setBackground(ContextCompat.getDrawable(this, R.color.mainGold));
            lblNightMode.setTypeface(null, Typeface.BOLD);
            lblDayMode.setBackground(null);
            lblDayMode.setTypeface(null, Typeface.NORMAL);
        } else {
            lblDayMode.setBackground(ContextCompat.getDrawable(this, R.color.mainGold));
            lblDayMode.setTypeface(null, Typeface.BOLD);
            lblNightMode.setBackground(null);
            lblNightMode.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void initializeLanguageButtons() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String lang = preferences.getString("language", "tr");
        if(lang.equals("tr")) {
            lblTurkish.setBackground(ContextCompat.getDrawable(getBaseContext(), R.color.mainGold));
            lblTurkish.setTypeface(null, Typeface.BOLD);
            lblEnglish.setBackground(null);
            lblEnglish.setTypeface(null, Typeface.NORMAL);
        } else {
            lblEnglish.setBackground(ContextCompat.getDrawable(getBaseContext(), R.color.mainGold));
            lblEnglish.setTypeface(null, Typeface.BOLD);
            lblTurkish.setBackground(null);
            lblTurkish.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void saveDisplayMode(boolean night_mode) {
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putBoolean("night_mode", night_mode);
        editor.apply();
    }

    private void updateDisplayModeLabel(boolean night_mode) {
        if (night_mode) {
            lblNightMode.setBackground(ContextCompat.getDrawable(this, R.color.mainGold));
            lblNightMode.setTypeface(null, Typeface.BOLD);
            lblDayMode.setBackground(null);
            lblDayMode.setTypeface(null, Typeface.NORMAL);
        } else {
            lblDayMode.setBackground(ContextCompat.getDrawable(this, R.color.mainGold));
            lblDayMode.setTypeface(null, Typeface.BOLD);
            lblNightMode.setBackground(null);
            lblNightMode.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateLanguageLabel(String lang) {
        switch (lang) {
            case "tr":
                lblTurkish.setBackground(ContextCompat.getDrawable(getBaseContext(), R.color.mainGold));
                lblTurkish.setTypeface(null, Typeface.BOLD);
                lblEnglish.setBackground(null);
                lblEnglish.setTypeface(null, Typeface.NORMAL);
                break;
            case "en":
                lblEnglish.setBackground(ContextCompat.getDrawable(getBaseContext(), R.color.mainGold));
                lblEnglish.setTypeface(null, Typeface.BOLD);
                lblTurkish.setBackground(null);
                lblTurkish.setTypeface(null, Typeface.NORMAL);
                break;
        }
    }

    private void saveLocale(String lang) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", lang);
        editor.apply();
    }

    @Override
    protected void attachBaseContext(Context newBase) { // on recreate updates locale, since display mode resets language to default!

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String LANG_CURRENT = preferences.getString("language", "tr");

        super.attachBaseContext(MyContextWrapper.wrap(newBase, LANG_CURRENT));
    }

    @Override
    public void onBackPressed() {
        btnBack.performClick();
    }

}