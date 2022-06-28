package com.arsinex.com.aboutUsPackage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arsinex.com.R;

public class AboutUsActivity extends AppCompatActivity {

    private final static String INSTAGRAM_PACKAGE = "com.instagram.android";
    private final static String TWITTER_PACKAGE = "com.twitter.android";
    private final static String YOUTUBE_PACKAGE = "com.google.android.youtube";

    private final static String FACEBOOK_APP = "fb://page/747732942285482";
    private final static String FACEBOOK_URL = "http://facebook.com/arsinexofficial";
    private final static String INSTA_APP = "http://instagram.com/arsinexcy";
    private final static String INSTA_URL = "http://instagram.com/_u/arsinexcy";
    private final static String TWITTER_URL = "https://twitter.com/@arsinexofficial";
    private final static String YOUTUBE_URL = "https://www.youtube.com/channel/UCBYs3RvelleE6s8Z4WiphgA";
    private final static String LINKEDIN_URL = "https://www.linkedin.com/company/arsinexofficial/";

    private TextView lblVersion;
    private ImageView btnBack, imgFacebook, imgTwitter, imgInstagram, imgYoutube, imgLinkedin, imgTelegram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_v2);

        imgFacebook = (ImageView) findViewById(R.id.imgFacebook);
        imgTwitter = (ImageView) findViewById(R.id.imgTwitter);
        imgInstagram = (ImageView) findViewById(R.id.imgInstagram);
        imgYoutube = (ImageView) findViewById(R.id.imgYoutube);
        imgLinkedin = (ImageView) findViewById(R.id.imgLinkedin);
        imgTelegram = (ImageView) findViewById(R.id.imgTelegram);
        lblVersion = (TextView) findViewById(R.id.lblVersion);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        checkVersion();

        imgFacebook.setOnClickListener(view ->  {
            goToFaceBook();
        });

        imgInstagram.setOnClickListener(view -> {
            goToInstagram();
        });

        imgTwitter.setOnClickListener(view -> {
            goToTwitter();
        });

        imgYoutube.setOnClickListener(view -> {
            goToYouTube();
        });

        imgLinkedin.setOnClickListener(view -> {
            goToLinkedin();
        });

        imgTelegram.setOnClickListener(view -> {
            goToTelegram();
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void checkVersion() {
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = packageInfo.versionName;
            lblVersion.setText("Version\t" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void goToFaceBook() {
        try {
            if (isAppInstalled(this, "com.facebook.orca")
                    || isAppInstalled(this, "com.facebook.katana")
                    || isAppInstalled(this, "com.example.facebook")
                    || isAppInstalled(this, "com.facebook.android")) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_APP)));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL)));
            }
        }catch (Exception e){e.printStackTrace();}
    }

    private void goToInstagram() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(INSTA_APP));
            intent.setPackage(INSTAGRAM_PACKAGE);
            startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTA_URL)));
        }
    }

    private void goToTwitter() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(TWITTER_URL));
            intent.setPackage(TWITTER_PACKAGE);
            startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(TWITTER_URL)));
        }
    }

    private void goToYouTube() {
        try {
            Intent intent =new Intent(Intent.ACTION_VIEW);
            intent.setPackage(YOUTUBE_PACKAGE);
            intent.setData(Uri.parse(YOUTUBE_URL));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URL)));
        }
    }

    private void goToLinkedin() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(LINKEDIN_URL));
        startActivity(intent);
    }

    private void goToTelegram() {
        Intent telegramIntent = new Intent(Intent.ACTION_VIEW , Uri.parse("https://telegram.me/arsinexofficial"));
        startActivity(telegramIntent);
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}