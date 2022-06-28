package com.arsinex.com.Discover;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

public class ArticleWebView extends AppCompatActivity {

    private WebView articleWebView;
    private ImageView btnBack;

    private Dialog pleaseWaitDialog;

    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_web_view);

        articleWebView = findViewById(R.id.articleWebView);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        pleaseWaitDialog = new Dialog(this);
        utils.setupDialog(pleaseWaitDialog, this);
        pleaseWaitDialog.show();

        String url = getIntent().getExtras().getString("url");
        articleWebView.getSettings().setJavaScriptEnabled(true);
        articleWebView.getSettings().setLoadWithOverviewMode(true);
        articleWebView.getSettings().setUseWideViewPort(true);

        articleWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pleaseWaitDialog.dismiss();
            }
        });

        articleWebView.loadUrl(url);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}