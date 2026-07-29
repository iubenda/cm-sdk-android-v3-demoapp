package net.consentmanager.kmm.cmpsdkdemoapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import net.consentmanager.kmm.cmpsdkdemoapp.debug.IabStorageDebugHelper

private const val DEBUG_LOG_TAG = "CmpDebug"

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}

@Composable
fun DebugScreen(
    onLog: (String) -> Unit,
    onOperationSuccess: () -> Unit = {},
) {
    val context = LocalContext.current
    var cmpBusy by remember { mutableStateOf(false) }
    var adBusy by remember { mutableStateOf(false) }
    var mutationBusy by remember { mutableStateOf(false) }
    var dumpBusy by remember { mutableStateOf(false) }
    var nextOperationId by remember { mutableIntStateOf(0) }

    fun nextId(): Int = ++nextOperationId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IosDemoPalette.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DemoButton(
            text = stringResource(R.string.debug_load_ad),
            containerColor = IosDemoPalette.indigo,
            isLoading = adBusy,
            onClick = {
                val operationId = nextId()
                adBusy = true
                onLog("ADMOB#$operationId Button pressed")
                onLog("ADMOB#$operationId MobileAds.initialize start")
                MobileAds.initialize(context) { initializationStatus ->
                    onLog(
                        "ADMOB#$operationId MobileAds.initialize complete " +
                            "adapters=${initializationStatus.adapterStatusMap}",
                    )
                    onLog(
                        "ADMOB#$operationId InterstitialAd.load start " +
                            "adUnitId=${BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID}",
                    )
                    InterstitialAd.load(
                        context,
                        BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: InterstitialAd) {
                                onLog(
                                    "ADMOB#$operationId onAdLoaded " +
                                        "responseInfo=${ad.responseInfo}",
                                )
                                ad.fullScreenContentCallback =
                                    object : FullScreenContentCallback() {
                                        override fun onAdShowedFullScreenContent() {
                                            onLog(
                                                "ADMOB#$operationId " +
                                                    "onAdShowedFullScreenContent",
                                            )
                                        }

                                        override fun onAdImpression() {
                                            onLog("ADMOB#$operationId onAdImpression")
                                        }

                                        override fun onAdClicked() {
                                            onLog("ADMOB#$operationId onAdClicked")
                                        }

                                        override fun onAdDismissedFullScreenContent() {
                                            onLog(
                                                "ADMOB#$operationId " +
                                                    "onAdDismissedFullScreenContent",
                                            )
                                            adBusy = false
                                            onLog(
                                                "ADMOB#$operationId " +
                                                    "Button operation finished",
                                            )
                                        }

                                        override fun onAdFailedToShowFullScreenContent(
                                            error: AdError,
                                        ) {
                                            onLog(
                                                "ADMOB#$operationId " +
                                                    "onAdFailedToShowFullScreenContent " +
                                                    "code=${error.code} " +
                                                    "domain=${error.domain} " +
                                                    "message=${error.message}",
                                            )
                                            adBusy = false
                                            onLog(
                                                "ADMOB#$operationId " +
                                                    "Button operation finished",
                                            )
                                        }
                                    }
                                val activity = context.findActivity()
                                if (activity == null) {
                                    onLog(
                                        "ADMOB#$operationId show skipped: " +
                                            "no Activity context",
                                    )
                                    adBusy = false
                                    onLog(
                                        "ADMOB#$operationId Button operation finished",
                                    )
                                    return
                                }
                                onLog("ADMOB#$operationId show start")
                                ad.show(activity)
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                onLog(
                                    "ADMOB#$operationId onAdFailedToLoad " +
                                        "code=${error.code} domain=${error.domain} " +
                                        "message=${error.message} " +
                                        "responseInfo=${error.responseInfo}",
                                )
                                Log.w(DEBUG_LOG_TAG, "Interstitial failed: ${error.message}")
                                adBusy = false
                                onLog("ADMOB#$operationId Button operation finished")
                            }
                        },
                    )
                    onLog("ADMOB#$operationId InterstitialAd.load invoked")
                }
            },
        )

        DemoButton(
            text = stringResource(R.string.debug_corrupt_iab),
            containerColor = IosDemoPalette.red,
            isLoading = mutationBusy,
            onClick = {
                val operationId = nextId()
                mutationBusy = true
                onLog("IAB#$operationId Button pressed")
                try {
                    val result = IabStorageDebugHelper.corruptIabStorage(
                        IabStorageDebugHelper.preferences(context),
                    )
                    IabStorageDebugHelper.formatCorruptLogLines(result).forEach(onLog)
                    if (!result.committed) {
                        onLog("IAB#$operationId SharedPreferences commit returned false")
                    } else {
                        onOperationSuccess()
                    }
                } catch (error: Throwable) {
                    onLog("IAB#$operationId Operation failed: ${error.message}")
                    Log.e(DEBUG_LOG_TAG, "corruptIabStorage failed", error)
                } finally {
                    mutationBusy = false
                    onLog("IAB#$operationId Button operation finished")
                }
            },
        )
    }
}
