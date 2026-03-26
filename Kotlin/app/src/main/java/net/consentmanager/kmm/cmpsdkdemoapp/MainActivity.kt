package net.consentmanager.kmm.cmpsdkdemoapp

import android.content.res.Configuration
import net.consentmanager.kmm.cmpsdkdemoapp.BuildConfig
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import net.consentmanager.cm_sdk_android_v3.CMPManager
import net.consentmanager.cm_sdk_android_v3.CMPManagerDelegate
import net.consentmanager.cm_sdk_android_v3.UrlConfig

/** Intent extra to skip ConfigurationScreen (for UI tests). */
const val EXTRA_SKIP_CONFIG = "skip_config"

class MainActivity : ComponentActivity(), CMPManagerDelegate {
    private var cmpManager: CMPManager? = null
    private lateinit var analytics: FirebaseAnalytics

    private var configuration by mutableStateOf(CMPConfiguration.default)
    private var hasConfiguration by mutableStateOf(false)
    private var isConsentInitialized by mutableStateOf(false)

    private val skipConfig: Boolean
        get() = intent?.getBooleanExtra(EXTRA_SKIP_CONFIG, false) == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        analytics = Firebase.analytics

        if (skipConfig) {
            hasConfiguration = true
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        isConsentInitialized -> {
                            cmpManager?.let { CMPDemoScreen(it) }
                        }
                        hasConfiguration -> {
                            ConsentLoadingScreen(onInit = { initCMPAndOpenConsent() })
                        }
                        else -> {
                            ConfigurationScreen(
                                configuration = configuration,
                                onConfigurationChange = { configuration = it },
                                onLoadCMP = {
                                    if (configuration.isValid) {
                                        hasConfiguration = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ConsentLoadingScreen(onInit: () -> Unit) {
        LaunchedEffect(Unit) { onInit() }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    private fun initCMPAndOpenConsent() {
        if (cmpManager != null) {
            checkAndOpenConsentLayer()
            return
        }
        if (!configuration.isValid) return
        val urlConfig = configuration.toUrlConfig()
        val webViewConfig = configuration.toConsentLayerUIConfig()
        cmpManager = CMPManager.getInstance(
            context = this,
            urlConfig = urlConfig,
            webViewConfig = webViewConfig,
            delegate = this
        )
        cmpManager!!.setActivity(this)
        checkAndOpenConsentLayer()
    }

    private fun checkAndOpenConsentLayer() {
        cmpManager?.checkAndOpen(false) { result ->
            result.onSuccess {
                runOnUiThread { isConsentInitialized = true }
            }.onFailure { error ->
                if (BuildConfig.DEBUG) {
                    Log.e("DemoApp", "Check and open consent layer failed with error: $error")
                }
            }
        }
    }

    private fun showCMPDemoScreen() {
        isConsentInitialized = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (BuildConfig.DEBUG) Log.d("CMP DemoApp", "Configuration changed")
        super.onConfigurationChanged(newConfig)
        cmpManager?.onApplicationResume()
    }

    override fun onPause() {
        super.onPause()
        cmpManager?.onApplicationPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        cmpManager?.onActivityDestroyed()
    }

    override fun didShowConsentLayer() {
        if (BuildConfig.DEBUG) Log.d("CMP DemoApp", "Consent Layer open message received.")
    }

    override fun didCloseConsentLayer() {
        if (BuildConfig.DEBUG) Log.d("CMP DemoApp", "Consent Layer close message received.")
        runOnUiThread { showCMPDemoScreen() }
    }

    override fun didReceiveConsent(consent: String, jsonObject: Map<String, Any>) {
        if (BuildConfig.DEBUG) Log.d("CMP DemoApp", "Consent Layer successfully received consent message.")
        runOnUiThread { showCMPDemoScreen() }
    }

    override fun didReceiveError(error: String) {
        if (BuildConfig.DEBUG) Log.e("CMP DemoApp", "SDK error: $error")
    }
}
