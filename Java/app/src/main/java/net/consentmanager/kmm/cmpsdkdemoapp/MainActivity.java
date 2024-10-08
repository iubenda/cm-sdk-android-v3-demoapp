package net.consentmanager.kmm.cmpsdkdemoapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import net.consentmanager.cm_sdk_android_v3.CMPManagerDelegate;
import net.consentmanager.cm_sdk_android_v3.ConsentLayerUIConfig;
import net.consentmanager.cm_sdk_android_v3.UrlConfig;
import net.consentmanager.java.JavaCMPManager;

import kotlinx.serialization.json.JsonObject;

public class MainActivity extends ComponentActivity implements CMPManagerDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmp_demo);

        UrlConfig urlConfig = new UrlConfig(
                "09cb5dca91e6b",
                "delivery.consentmanager.net",
                "EN",
                "CMDemoAppJava"
        );

        ConsentLayerUIConfig webViewConfig = new ConsentLayerUIConfig(
                ConsentLayerUIConfig.Position.HALF_SCREEN_BOTTOM,
                ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.BLACK, 0.5f),
                10f,
                true,
                false
        );

        JavaCMPManager cmpManager = JavaCMPManager.getInstance(this, urlConfig, webViewConfig, this);
        cmpManager.setActivity(this);

        cmpManager.checkWithServerAndOpenIfNecessary(result -> {
            if (result.isSuccess()) {
                showCMPDemoScreen();
            } else {
                Log.e("JavaDemoAp", "Initialize method failed with error: " + result.exceptionOrNull());
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
        Log.d("CMP JavaDemoAp", "Consent Layer open message received.");
    }

    @Override
    public void didCloseConsentLayer() {
        Log.d("CMP JavaDemoAp", "Consent Layer close message received.");
    }

    @Override
    public void didReceiveError(@NonNull String error) {
        Log.e("CMP JavaDemoAp", "SDK error: " + error);
    }

    @Override
    public void didReceiveConsent(@NonNull String consent, @NonNull JsonObject jsonObject) {
        Log.d("CMP JavaDemoAp", "Consent received: " + consent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void addMenuProvider(@NonNull MenuProvider provider, @NonNull LifecycleOwner owner, @NonNull Lifecycle.State state) {

    }
}
