package com.habeshastudio.fooddelivery.activities.profile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;

public class About extends AppCompatActivity {

    WebView  myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        myWebView = findViewById(R.id.web_view_about);
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
        fileList();
    }
}
