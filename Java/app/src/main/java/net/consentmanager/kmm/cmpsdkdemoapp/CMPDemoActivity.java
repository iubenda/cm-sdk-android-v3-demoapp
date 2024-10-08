package net.consentmanager.kmm.cmpsdkdemoapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import net.consentmanager.cm_sdk_android_v3.CMPManagerDelegate;
import net.consentmanager.cm_sdk_android_v3.ConsentLayerUIConfig;
import net.consentmanager.cm_sdk_android_v3.UrlConfig;
import net.consentmanager.java.JavaCMPManager;

import java.util.Arrays;

import kotlinx.serialization.json.JsonObject;

public class CMPDemoActivity extends ComponentActivity implements CMPManagerDelegate {

    private JavaCMPManager cmpManager;
    private Handler mainHandler;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmp_demo);

        mainHandler = new Handler(Looper.getMainLooper());
        webView = new WebView(this);

        UrlConfig urlConfig = new UrlConfig(
                "Your Code ID goes here",  // TODO: replace this with the Code ID from your CMP
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

        cmpManager = JavaCMPManager.getInstance(this, urlConfig, webViewConfig, this);
        cmpManager.setActivity(this);
        cmpManager.setWebView(webView);

        setupButtons();
    }

    private void setupButtons() {
        setupButton(R.id.btnHasConsent, v -> checkHasUserChoice());
        setupButton(R.id.btnHasPurpose, v -> checkHasPurpose());
        setupButton(R.id.btnHasVendor, v -> checkHasVendor());
        setupButton(R.id.btnGetCmpString, v -> getCmpString());
        setupButton(R.id.btnGetAllPurposes, v -> getAllPurposes());
        setupButton(R.id.btnGetEnabledPurposes, v -> getEnabledPurposes());
        setupButton(R.id.btnGetDisabledPurposes, v -> getDisabledPurposes());
        setupButton(R.id.btnGetAllVendors, v -> getAllVendors());
        setupButton(R.id.btnGetEnabledVendors, v -> getEnabledVendors());
        setupButton(R.id.btnGetDisabledVendors, v -> getDisabledVendors());
        setupButton(R.id.btnCheckAndOpenConsentLayer, v -> checkAndOpenConsentLayer());
        setupButton(R.id.btnCheckConsentRequired, v -> checkConsentRequired());
        setupButton(R.id.btnEnableVendorList, v -> enableVendorList());
        setupButton(R.id.btnDisableVendorList, v -> disableVendorList());
        setupButton(R.id.btnEnablePurposeList, v -> enablePurposeList());
        setupButton(R.id.btnDisablePurposeList, v -> disablePurposeList());
        setupButton(R.id.btnRejectAll, v -> rejectAll());
        setupButton(R.id.btnAcceptAll, v -> acceptAll());
        setupButton(R.id.btnReset, v -> reset());
        setupButton(R.id.btnOpenConsentLayer, v -> openConsentLayer());
    }

    private void setupButton(int buttonId, View.OnClickListener listener) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(listener);
    }

    private void checkHasUserChoice() {
        boolean hasConsent = cmpManager.hasUserChoice();
        showToast("Has Consent: " + hasConsent);
    }

    private void checkHasPurpose() {
        boolean hasPurpose = cmpManager.hasPurposeConsent("c53");
        showToast("Has Purpose c53: " + hasPurpose);
    }

    private void checkHasVendor() {
        boolean hasVendor = cmpManager.hasVendorConsent("s2789");
        showToast("Has Vendor s2789: " + hasVendor);
    }

    private void getCmpString() {
        String cmpString = cmpManager.exportCMPInfo();
        showToast("CMP String: " + cmpString);
    }

    private void getAllPurposes() {
        String allPurposes = Arrays.toString(cmpManager.getAllPurposesIDs().toArray());
        showToast("All Purposes: " + allPurposes);
    }

    private void getEnabledPurposes() {
        String enabledPurposes = Arrays.toString(cmpManager.getEnabledPurposesIDs().toArray());
        showToast("Enabled Purposes: " + enabledPurposes);
    }

    private void getDisabledPurposes() {
        String disabledPurposes = Arrays.toString(cmpManager.getDisabledPurposesIDs().toArray());
        showToast("Disabled Purposes: " + disabledPurposes);
    }

    private void getAllVendors() {
        String allVendors = Arrays.toString(cmpManager.getAllVendorsIDs().toArray());
        showToast("All Vendors: " + allVendors);
    }

    private void getEnabledVendors() {
        String enabledVendors = Arrays.toString(cmpManager.getEnabledVendorsIDs().toArray());
        showToast("Enabled Vendors: " + enabledVendors);
    }

    private void getDisabledVendors() {
        String disabledVendors = Arrays.toString(cmpManager.getDisabledVendorsIDs().toArray());
        showToast("Disabled Vendors: " + disabledVendors);
    }

    private void reset() {
        cmpManager.resetConsentManagementData();
        showToast("All Consent Data Deleted");
    }

    private void checkAndOpenConsentLayer() {
        cmpManager.checkWithServerAndOpenIfNecessary(result -> {
            if (result.isSuccess()) {
                showToast("Check and Open Consent Layer operation done successfully.");
            } else {
                showToast("Check and Open Consent Layer operation failed: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void checkConsentRequired() {
        cmpManager.checkIfConsentIsRequired(needsConsent -> showToast("Needs Consent: " + needsConsent));
    }

    private void enableVendorList() {
        cmpManager.acceptVendors(Arrays.asList("s2790", "s2791"), result -> {
            if (result.isSuccess()) {
                showToast("Vendors Enabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void disableVendorList() {
        cmpManager.rejectVendors(Arrays.asList("s2790", "s2791"), result -> {
            if (result.isSuccess()) {
                showToast("Vendors Disabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void enablePurposeList() {
        cmpManager.acceptPurposes(Arrays.asList("c51", "c52"), true, result -> {
            if (result.isSuccess()) {
                showToast("Purposes enabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void disablePurposeList() {
        cmpManager.rejectPurposes(Arrays.asList("c51", "c52"), true, result -> {
            if (result.isSuccess()) {
                showToast("Purposes disabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void rejectAll() {
        cmpManager.rejectAll(result -> {
            if (result.isSuccess()) {
                showToast("All consents rejected");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void acceptAll() {
        cmpManager.acceptAll(result -> {
            if (result.isSuccess()) {
                showToast("All consents accepted");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void openConsentLayer() {
        cmpManager.openConsentLayer(result -> {
            if (!result.isSuccess()) {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(CMPDemoActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void didReceiveConsent(@NonNull String consent, @NonNull JsonObject jsonObject) {
        Log.d("CMP JavaDemoApp", "Consent received: " + consent);
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
        Log.e("CMP JavaDemoApp", "Error received: " + error);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}