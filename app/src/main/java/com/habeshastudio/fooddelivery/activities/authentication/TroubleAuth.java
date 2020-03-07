package com.habeshastudio.fooddelivery.activities.authentication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;

public class TroubleAuth extends AppCompatActivity {

    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_auth);
        myWebView = findViewById(R.id.web_auth_help_view);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        myWebView.loadUrl(Common.AboutUrl);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
