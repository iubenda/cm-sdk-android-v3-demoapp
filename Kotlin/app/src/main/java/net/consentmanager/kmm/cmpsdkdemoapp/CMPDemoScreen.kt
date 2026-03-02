package net.consentmanager.kmm.cmpsdkdemoapp

import android.preference.PreferenceManager
import net.consentmanager.kmm.cmpsdkdemoapp.BuildConfig
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.consentmanager.cm_sdk_android_v3.CMPManager

@Composable
fun CMPDemoScreen(cmpManager: CMPManager) {
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CMP Manager Methods",
                style = MaterialTheme.typography.headlineMedium
            )

            DemoButton(
                text = "Check User Status",
                onClick = {
                    val status = cmpManager.getUserStatus()
                    if (BuildConfig.DEBUG) {
                        Log.d("CMPDemo", "User Status: ${status.hasUserChoice}")
                        Log.d("CMPDemo", "TCF: ${status.tcf}")
                        Log.d("CMPDemo", "Additional Consent: ${status.addtlConsent}")
                        Log.d("CMPDemo", "Regulation: ${status.regulation}")
                        Log.d("CMPDemo", "---- Vendors Status ----")
                        status.vendors.forEach { (vendorId, choice) ->
                            Log.d("CMPDemo", "Vendor $vendorId: $choice")
                        }
                        Log.d("CMPDemo", "---- Purposes Status ----")
                        status.purposes.forEach { (purposeId, choice) ->
                            Log.d("CMPDemo", "Purpose $purposeId: $choice")
                        }
                    }
                    toastMessage = "Check Logcat for User Status"
                }
            )

            DemoButton(
                text = "Has Purpose ID c53?",
                onClick = {
                    val hasPurpose = cmpManager.getStatusForPurpose("c53")
                    toastMessage = "Has Purpose: $hasPurpose"
                }
            )

            DemoButton(
                text = "Has Vendor ID s2789?",
                onClick = {
                    val hasVendor = cmpManager.getStatusForVendor("s2789")
                    toastMessage = "Has Vendor: $hasVendor"
                }
            )

            DemoButton(
                text = "Get CMP String",
                onClick = {
                    val cmpString = cmpManager.exportCMPInfo()
                    if (BuildConfig.DEBUG) Log.d("CMPDemo", "Exported CMP String: $cmpString")
                    val display = if (cmpString.length > 50) cmpString.take(50) + "…" else cmpString
                    toastMessage = "CMP String: $display"
                }
            )

            DemoButton(
                text = "Check and Open Consent Layer",
                onClick = {
                    cmpManager.checkAndOpen() { result ->
                        result.onSuccess {
                            toastMessage =
                                "Check and Open Consent Layer operation done successfully."
                        }.onFailure { error ->
                            toastMessage =
                                "Check and Open Consent Layer operation failed with error: $error"
                        }
                    }
                }
            )

            DemoButton(
                text = "Enable Vendors s2790 and s2791",
                onClick = {
                    cmpManager.acceptVendors(listOf("s2790", "s2791")) { result ->
                        result.onSuccess {
                            toastMessage = "Vendors Enabled"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Disable Vendors s2790 and s2791",
                onClick = {
                    cmpManager.rejectVendors(listOf("s2790", "s2791")) { result ->
                        result.onSuccess {
                            toastMessage = "Vendors Disabled"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Enable Purposes c52 and c53",
                onClick = {
                    cmpManager.acceptPurposes(listOf("c52", "c53"), true) { result ->
                        result.onSuccess {
                            toastMessage = "Purposes enabled"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Disable Purposes c52 and c53",
                onClick = {
                    cmpManager.rejectPurposes(listOf("c52", "c53"), true) { result ->
                        result.onSuccess {
                            toastMessage = "Purposes disabled"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Reject All",
                onClick = {
                    cmpManager.rejectAll { result ->
                        result.onSuccess {
                            toastMessage = "All consents rejected"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Accept All",
                onClick = {
                    cmpManager.acceptAll { result ->
                        result.onSuccess {
                            toastMessage = "All consents accepted"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Open Consent Layer",
                onClick = {
                    cmpManager.forceOpen() { result ->
                        result.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Jump to Settings Page",
                onClick = {
                    cmpManager.forceOpen(jumpToSettings = true) { result ->
                        result.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Reset",
                onClick = {
                    cmpManager.resetConsentManagementData()
                    toastMessage = "Consent data reset"
                }
            )

            DemoButton(
                text = "Import CMP String",
                onClick = {
                    cmpManager.importCMPInfo("Q1FMVW10Z1FMVW10Z0FmUTVDSVRCWUZnQUFBQUFBQUFBQWlnS3dOWF9HX19iWGx2LVg3MzZmdGtlWTFmOTloNzdzUXhCaGZKcy00RnpMdldfSndYMzJFek5FMzZ0cVlLbVJJQXUzVEJJUU50R0pqVVJWQ2hhb2dWcnpEc2FFeVVvVHRLSi1Ca2lITVJZMmRZQ0Z4dm00dGplUUNaNXZyXzkxZDUyUl90N2RyLTNkenl5NWhudjNhOV8tUzFXSmlkSzUtdEhfdjliUk9iLV9JLTlfeC1fNHY0X05fcEUyX2VUMXRfdFd2dDczOS04dHZfOV9fOTlfX19fZl9fX19fXzNfLV9mX19mX19fOEZYd0NURFFxSUF5d0pDUWcwRENDQkFDb0t3Z0lvRUFRQUFKQTBRRUFKZ3dLZGdZQUxyQ1JBQ0FGQUFNRUFJQUFRWkFBZ0FBQWdBUWlBQ0FBb0VBQUVBZ1VBQVlBRUF3RUFCQXdBQWdBc0JBSUFBUUhRTVV3SUlCQXNBRWpNaW9Vd0lRZ0VnZ0piS2hCSUFnUVZ3aENMUEFJZ0VSTUZBQUFBQUFVZ0FDQXNGZ2NTU0FsUWtFQVhFRzBBQUJBQWdFRUFCUWdrNU1BQVFCbXkxQjRNRzBaV21BWVBtQ1JEVEFNZ0NJSXlFZzBBQUEjXzUxXzUyXzUzXzU0XzU1XzU2XyNfczI4MTVfYzY0MDQzX3MyODE0X3MyNzYyX3MyODg1X3MyODE5X3MyODQ2X3MzMDM1X3MyNDM0X1VfIzEtLS0j") { result ->
                        result.onSuccess {
                            toastMessage = "CMP String imported!"
                        }.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
                    }
                }
            )

            DemoButton(
                text = "Get Google Consent Mode Settings",
                onClick = {
                    val settings = cmpManager.getGoogleConsentModeStatus()
                    if (BuildConfig.DEBUG) Log.d("CMPDemo", "Google Consent Mode Settings: $settings")
                    toastMessage = buildString {
                        append("Google Consent Settings:")
                        settings.forEach { (key, value) ->
                            append("\n$key: $value")
                        }
                    }
                }
            )

            DemoButton(
                text = "Inspect SharedPreferences",
                onClick = {
                    toastMessage = "Check logs for the list of stored key/value pairs on SharedPreference"
                    if (BuildConfig.DEBUG) {
                        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        val allEntries = prefs.all
                        Log.d("CMPPrefsInspector", "=== Start of SharedPreferences Dump ===")
                        allEntries.forEach { (key, value) ->
                            val valueType = when (value) {
                                is String -> "String"
                                is Int -> "Integer"
                                is Boolean -> "Boolean"
                                is Float -> "Float"
                                is Long -> "Long"
                                is Set<*> -> "Set"
                                null -> "null"
                                else -> value.javaClass.simpleName
                            }
                            Log.d("CMPPrefsInspector", "Key: $key, Type: $valueType, Value: $value")
                        }
                        Log.d("CMPPrefsInspector", "=== End of SharedPreferences Dump ===")
                    }
                }
            )
        }

        toastMessage?.let { message ->
            Toast(message = message) {
                toastMessage = null
            }
        }
    }
}

@Composable
fun DemoButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
fun Toast(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}