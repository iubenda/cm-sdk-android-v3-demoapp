package net.consentmanager.kmm.cmpsdkdemoapp

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonObject
import net.consentmanager.cm_sdk_android_v3.CMPManager
import net.consentmanager.cm_sdk_android_v3.CMPManagerDelegate
import net.consentmanager.cm_sdk_android_v3.ConsentLayerUIConfig
import net.consentmanager.cm_sdk_android_v3.UrlConfig

class MainActivity : ComponentActivity(), CMPManagerDelegate {
    private lateinit var cmpManager: CMPManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val urlConfig = UrlConfig(
            id = "09cb5dca91e6b",
            domain = "delivery.consentmanager.net",
            language = "EN",
            appName = "CMDemoAppKotlin"
        )

        val webViewConfig = ConsentLayerUIConfig(
            position = ConsentLayerUIConfig.Position.HALF_SCREEN_BOTTOM,
            backgroundStyle = ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.BLACK, 0.5f),
            cornerRadius = 10f,
            respectsSafeArea = true,
            isCancelable = false
        )

        cmpManager = CMPManager.getInstance(
            context = this,
            urlConfig = urlConfig,
            webViewConfig = webViewConfig,
            delegate = this
        )

        cmpManager.setActivity(this)

        checkAndOpenConsentLayer()
    }

    private fun checkAndOpenConsentLayer() {
        cmpManager.checkWithServerAndOpenIfNecessary { result ->
            result.onSuccess {
                showCMPDemoScreen()
            }.onFailure { error ->
                Log.e("DemoApp", "Check and open consent layer failed with error: $error")
            }
        }
    }

    private fun showCMPDemoScreen() {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CMPDemoScreen(cmpManager)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("CMP DemoApp", "Configuration changed")
        super.onConfigurationChanged(newConfig)
        cmpManager.onApplicationResume()
    }

    //    override fun onResume() {
//        Log.d("CMP DemoApp", "Activity resumed")
//        super.onResume()
//        cmpManager.setActivity(this)
//        cmpManager.onApplicationResume()
//    }
//
    override fun onPause() {
        Log.d("CMP DemoApp", "Activity paused")
        super.onPause()
        cmpManager.onApplicationPause()
    }

    override fun onDestroy() {
        Log.d("CMP DemoApp", "Activity destroyed")
        super.onDestroy()
        cmpManager.onActivityDestroyed()
    }

    override fun didReceiveConsent(consent: String, jsonObject: JsonObject) {
        Log.d("CMP DemoApp", "Consent Layer successfully received consent message.")
        runOnUiThread {
            showCMPDemoScreen()
        }
    }

    override fun didShowConsentLayer() {
        Log.d("CMP DemoApp", "Consent Layer open message received.")
    }

    override fun didCloseConsentLayer() {
        Log.d("CMP DemoApp", "Consent Layer close message received.")
        runOnUiThread {
            showCMPDemoScreen()
        }
    }

    override fun didReceiveError(error: String) {
        Log.e("CMP DemoApp", "SDK error: $error")
    }
}
