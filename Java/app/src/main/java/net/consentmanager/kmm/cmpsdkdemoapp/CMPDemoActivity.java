package net.consentmanager.kmm.cmpsdkdemoapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
                ConsentLayerUIConfig.Position.HALF_SCREEN_BOTTOM, // TO-DO: not implemented yet
                ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.BLACK, 0.5f),
                10f,
                true,
                false
        );

        cmpManager = JavaCMPManager.getInstance(this, urlConfig, webViewConfig, this);
        cmpManager.setActivity(this);

        setupButtons();
    }

    private void setupButtons() {
        setupButton(R.id.btnHasConsent, this::checkHasUserChoice);
        setupButton(R.id.btnHasPurpose, this::checkHasPurpose);
        setupButton(R.id.btnHasVendor, this::checkHasVendor);
        setupButton(R.id.btnGetCmpString, this::getCmpString);
        setupButton(R.id.btnGetAllPurposes, this::getAllPurposes);
        setupButton(R.id.btnGetEnabledPurposes, this::getEnabledPurposes);
        setupButton(R.id.btnGetDisabledPurposes, this::getDisabledPurposes);
        setupButton(R.id.btnGetAllVendors, this::getAllVendors);
        setupButton(R.id.btnGetEnabledVendors, this::getEnabledVendors);
        setupButton(R.id.btnGetDisabledVendors, this::getDisabledVendors);
        setupButton(R.id.btnCheckAndOpenConsentLayer, this::checkAndOpenConsentLayer);
        setupButton(R.id.btnCheckConsentRequired, this::checkConsentRequired);
        setupButton(R.id.btnEnableVendorList, this::enableVendorList);
        setupButton(R.id.btnDisableVendorList, this::disableVendorList);
        setupButton(R.id.btnEnablePurposeList, this::enablePurposeList);
        setupButton(R.id.btnDisablePurposeList, this::disablePurposeList);
        setupButton(R.id.btnRejectAll, this::rejectAll);
        setupButton(R.id.btnAcceptAll, this::acceptAll);
        setupButton(R.id.btnReset, this::reset);
        setupButton(R.id.btnOpenConsentLayer, this::openConsentLayer);
    }

    private void setupButton(int buttonId, View.OnClickListener listener) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(listener);
    }

    private void checkHasUserChoice(View v) {
        boolean hasConsent = cmpManager.hasUserChoice();
        showToast("Has Consent: " + hasConsent);
    }

    private void checkHasPurpose(View v) {
        boolean hasPurpose = cmpManager.hasPurposeConsent("c53");
        showToast("Has Purpose c53: " + hasPurpose);
    }

    private void checkHasVendor(View v) {
        boolean hasVendor = cmpManager.hasVendorConsent("s2789");
        showToast("Has Vendor s2789: " + hasVendor);
    }

    private void getCmpString(View v) {
        String cmpString = cmpManager.exportCMPInfo();
        showToast("CMP String: " + cmpString);
    }

    private void getAllPurposes(View v) {
        String allPurposes = Arrays.toString(cmpManager.getAllPurposesIDs().toArray());
        showToast("All Purposes: " + allPurposes);
    }

    private void getEnabledPurposes(View v) {
        String enabledPurposes = Arrays.toString(cmpManager.getEnabledPurposesIDs().toArray());
        showToast("Enabled Purposes: " + enabledPurposes);
    }

    private void getDisabledPurposes(View v) {
        String disabledPurposes = Arrays.toString(cmpManager.getDisabledPurposesIDs().toArray());
        showToast("Disabled Purposes: " + disabledPurposes);
    }

    private void getAllVendors(View v) {
        String allVendors = Arrays.toString(cmpManager.getAllVendorsIDs().toArray());
        showToast("All Vendors: " + allVendors);
    }

    private void getEnabledVendors(View v) {
        String enabledVendors = Arrays.toString(cmpManager.getEnabledVendorsIDs().toArray());
        showToast("Enabled Vendors: " + enabledVendors);
    }

    private void getDisabledVendors(View v) {
        String disabledVendors = Arrays.toString(cmpManager.getDisabledVendorsIDs().toArray());
        showToast("Disabled Vendors: " + disabledVendors);
    }

    private void reset(View v) {
        cmpManager.resetConsentManagementData();
        showToast("All Consent Data Deleted");
    }

    private void checkAndOpenConsentLayer(View v) {
        cmpManager.checkWithServerAndOpenIfNecessary(result -> {
            if (result.isSuccess()) {
                showToast("Check and Open Consent Layer operation done successfully.");
            } else {
                showToast("Check and Open Consent Layer operation failed: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void checkConsentRequired(View v) {
        cmpManager.checkIfConsentIsRequired(needsConsent -> showToast("Needs Consent: " + needsConsent));
    }

    private void enableVendorList(View v) {
        cmpManager.acceptVendors(Arrays.asList("s2790", "s2791"), result -> {
            if (result.isSuccess()) {
                showToast("Vendors Enabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void disableVendorList(View v) {
        cmpManager.rejectVendors(Arrays.asList("s2790", "s2791"), result -> {
            if (result.isSuccess()) {
                showToast("Vendors Disabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void enablePurposeList(View v) {
        cmpManager.acceptPurposes(Arrays.asList("c51", "c52"), true, result -> {
            if (result.isSuccess()) {
                showToast("Purposes enabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void disablePurposeList(View v) {
        cmpManager.rejectPurposes(Arrays.asList("c51", "c52"), true, result -> {
            if (result.isSuccess()) {
                showToast("Purposes disabled");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void rejectAll(View v) {
        cmpManager.rejectAll(result -> {
            if (result.isSuccess()) {
                showToast("All consents rejected");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void acceptAll(View v) {
        cmpManager.acceptAll(result -> {
            if (result.isSuccess()) {
                showToast("All consents accepted");
            } else {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void openConsentLayer(View v) {
        cmpManager.openConsentLayer(result -> {
            if (!result.isSuccess()) {
                showToast("Error: " + result.exceptionOrNull());
            }
            return null;
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}