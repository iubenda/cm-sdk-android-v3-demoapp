package net.consentmanager.kmm.cmpsdkdemoapp

// CMPDemoScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.consentmanager.cm_sdk_android_v3.CMPManager

@Composable
fun CMPDemoScreen(cmpManager: CMPManager) {
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CMP Manager Methods",
                style = MaterialTheme.typography.headlineMedium
            )

            DemoButton(
                text = "Has User Choice?",
                onClick = {
                    val hasConsent = cmpManager.hasUserChoice()
                    toastMessage = "Has Consent: $hasConsent"
                }
            )

            DemoButton(
                text = "Has Purpose ID c53?",
                onClick = {
                    val hasPurpose = cmpManager.hasPurposeConsent("c53")
                    toastMessage = "Has Purpose: $hasPurpose"
                }
            )

            DemoButton(
                text = "Has Vendor ID s2789?",
                onClick = {
                    val hasVendor = cmpManager.hasVendorConsent("s2789")
                    toastMessage = "Has Vendor: $hasVendor"
                }
            )

            DemoButton(
                text = "Get CMP String",
                onClick = {
                    val cmpString = cmpManager.exportCMPInfo()
                    toastMessage = "CMP String: $cmpString"
                }
            )

            DemoButton(
                text = "Get All Purposes",
                onClick = {
                    val allPurposes = cmpManager.getAllPurposesIDs()
                    toastMessage = "All Purposes: $allPurposes"
                }
            )

            DemoButton(
                text = "Get Enabled Purposes",
                onClick = {
                    val enabledPurposes = cmpManager.getEnabledPurposesIDs()
                    toastMessage = "Enabled Purposes: $enabledPurposes"
                }
            )

            DemoButton(
                text = "Get Disabled Purposes",
                onClick = {
                    val disabledPurposes = cmpManager.getDisabledPurposesIDs()
                    toastMessage = "Disabled Purposes: ${disabledPurposes.joinToString(", ")}"
                }
            )

            DemoButton(
                text = "Get All Vendors",
                onClick = {
                    val allVendors = cmpManager.getAllVendorsIDs()
                    toastMessage = "All Vendors: $allVendors"
                }
            )

            DemoButton(
                text = "Get Enabled Vendors",
                onClick = {
                    val enabledVendors = cmpManager.getEnabledVendorsIDs()
                    toastMessage = "Enabled Vendors: $enabledVendors"
                }
            )

            DemoButton(
                text = "Get Disabled Vendors",
                onClick = {
                    val disabledVendors = cmpManager.getDisabledVendorsIDs()
                    toastMessage = "Disabled Vendors: ${disabledVendors.joinToString(", ")}"
                }
            )

            DemoButton(
                text = "Check and Open Consent Layer",
                onClick = {
                    cmpManager.checkWithServerAndOpenIfNecessary { result ->
                        result.onSuccess {
                            toastMessage = "Check and Open Consent Layer operation done successfully."
                        }.onFailure { error ->
                            toastMessage = "Check and Open Consent Layer operation failed with error: $error"
                        }
                    }
                }
            )

            DemoButton(
                text = "Check Consent Required",
                onClick = {
                    cmpManager.checkIfConsentIsRequired { needsConsent ->
                        toastMessage = "Needs Consent: $needsConsent"
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
                    cmpManager.openConsentLayer { result ->
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
                    cmpManager.openConsentLayer { result ->
                        result.onFailure { error ->
                            toastMessage = "Error: ${error.message}"
                        }
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(message)
        }
    }
}
