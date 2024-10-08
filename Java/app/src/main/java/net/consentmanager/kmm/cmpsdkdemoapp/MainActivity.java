package net.consentmanager.kmm.cmpsdkdemoapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import net.consentmanager.cm_sdk_android_v3.CMPManagerDelegate;
import net.consentmanager.cm_sdk_android_v3.ConsentLayerUIConfig;
import net.consentmanager.cm_sdk_android_v3.UrlConfig;
import net.consentmanager.java.JavaCMPManager;

import kotlinx.serialization.json.JsonObject;

import android.webkit.WebView;
import android.widget.FrameLayout;

public class MainActivity extends ComponentActivity implements CMPManagerDelegate {

    private JavaCMPManager cmpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        FrameLayout layout = new FrameLayout(this);
        layout.addView(webView);
        setContentView(layout);

        initializeCMPManager();
    }

    private void initializeCMPManager() {
        UrlConfig urlConfig = new UrlConfig(
                "09cb5dca91e6b",
                "delivery.consentmanager.net",
                "EN",
                "CMDemoAppJava"
        );

        ConsentLayerUIConfig webViewConfig = new ConsentLayerUIConfig(
                ConsentLayerUIConfig.Position.FULL_SCREEN,
                ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.BLACK, 0.5f),
                10f,
                true,
                false
        );

        WebView webView = new WebView(this);
        cmpManager = JavaCMPManager.getInstance(this, urlConfig, webViewConfig, this);
        cmpManager.setActivity(this);
        cmpManager.setWebView(webView);

        cmpManager.checkWithServerAndOpenIfNecessary(result -> {
            if (result.isSuccess()) {
                Log.d("JavaDemoApp", "CMP Initialized successfully");
            } else {
                Log.e("JavaDemoApp", "Initialize method failed with error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void showCMPDemoScreen() {
        Intent intent = new Intent(this, CMPDemoActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void didShowConsentLayer() {
        Log.d("CMP JavaDemoApp", "Consent Layer open message received.");
    }

    @Override
    public void didCloseConsentLayer() {
        Log.d("CMP JavaDemoApp", "Consent Layer close message received.");
    }

    @Override
    public void didReceiveError(@NonNull String error) {
        Log.e("CMP JavaDemoApp", "SDK error: " + error);
    }

    @Override
    public void didReceiveConsent(@NonNull String consent, @NonNull JsonObject jsonObject) {
        Log.d("CMP JavaDemoApp", "Consent received: " + consent);
        runOnUiThread(this::showCMPDemoScreen);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cmpManager != null) {
            cmpManager.onActivityDestroyed();
        }
    }
}