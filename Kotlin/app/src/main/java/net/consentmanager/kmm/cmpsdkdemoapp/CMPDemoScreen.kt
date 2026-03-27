package net.consentmanager.kmm.cmpsdkdemoapp

import android.content.Context
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.consentmanager.cm_sdk_android_v3.CMPManager

private object LoadingKeys {
    const val CHECK_AND_OPEN = "checkAndOpen"
    const val CONSENT_REQUIRED = "consentRequired"
    const val VENDORS_ENABLE = "vendorsEnable"
    const val VENDORS_DISABLE = "vendorsDisable"
    const val PURPOSES_ENABLE = "purposesEnable"
    const val PURPOSES_DISABLE = "purposesDisable"
    const val REJECT_ALL = "rejectAll"
    const val ACCEPT_ALL = "acceptAll"
    const val FORCE_OPEN = "forceOpen"
    const val JUMP_SETTINGS = "jumpSettings"
    const val IMPORT_CMP = "importCmp"
}

@Composable
fun CMPDemoScreen(
    cmpManager: CMPManager,
    onLog: (String) -> Unit
) {
    val context = LocalContext.current
    var loadingKeys by remember { mutableStateOf(setOf<String>()) }
    fun beginLoad(key: String) {
        loadingKeys = loadingKeys + key
    }
    fun endLoad(key: String) {
        loadingKeys = loadingKeys - key
    }
    fun isLoading(key: String) = key in loadingKeys

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IosDemoPalette.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CMP Manager Methods",
            style = MaterialTheme.typography.headlineMedium,
            color = IosDemoPalette.titleIndigo
        )

        DemoButton(
            text = "Check User Status",
            containerColor = IosDemoPalette.blue,
            onClick = {
                val status = cmpManager.getUserStatus()
                onLog(
                    buildString {
                        appendLine("Check User Status")
                        appendLine("hasUserChoice: ${status.hasUserChoice}")
                        appendLine("TCF: ${status.tcf}")
                        appendLine("Additional Consent: ${status.addtlConsent}")
                        appendLine("Regulation: ${status.regulation}")
                        appendLine("Vendors:")
                        status.vendors.forEach { (vendorId, choice) ->
                            appendLine("  $vendorId: $choice")
                        }
                        appendLine("Purposes:")
                        status.purposes.forEach { (purposeId, choice) ->
                            appendLine("  $purposeId: $choice")
                        }
                    }.trim()
                )
            }
        )

        DemoButton(
            text = "Get CMP String",
            containerColor = IosDemoPalette.teal,
            onClick = {
                val cmpString = cmpManager.exportCMPInfo()
                onLog("CMP String:\n$cmpString")
            }
        )

        DemoButton(
            text = "Check and Open Consent Layer",
            containerColor = IosDemoPalette.indigo,
            isLoading = isLoading(LoadingKeys.CHECK_AND_OPEN),
            onClick = {
                beginLoad(LoadingKeys.CHECK_AND_OPEN)
                cmpManager.checkAndOpen() { result ->
                    endLoad(LoadingKeys.CHECK_AND_OPEN)
                    result.onSuccess {
                        onLog("Check and Open Consent Layer: success")
                    }.onFailure { error ->
                        onLog("Check and Open Consent Layer failed: $error")
                    }
                }
            }
        )

        DemoButton(
            text = "Check Consent Required",
            containerColor = IosDemoPalette.indigo,
            isLoading = isLoading(LoadingKeys.CONSENT_REQUIRED),
            onClick = {
                beginLoad(LoadingKeys.CONSENT_REQUIRED)
                cmpManager.isConsentRequired { result ->
                    endLoad(LoadingKeys.CONSENT_REQUIRED)
                    result.onSuccess { required ->
                        onLog("Consent required: $required")
                    }.onFailure { error ->
                        onLog("isConsentRequired error: ${error.message}")
                    }
                }
            }
        )

        ConsentMultiSelectRow(
            cmpManager = cmpManager,
            onLog = onLog,
            actionButtonText = "Enable",
            actionButtonColor = IosDemoPalette.cyan,
            isActionLoading = isLoading(LoadingKeys.VENDORS_ENABLE),
            selectPromptWhenEmpty = "Select vendors…",
            emptyDropdownMessage = "No vendors in current status.",
            emptySelectionLogMessage = "Enable vendors: select one or more vendors in the list first.",
            errorLogPrefix = "Enable vendors error",
            successMessage = { ids -> "Vendors enabled: ${ids.joinToString()}" },
            refreshIds = { m -> m.getUserStatus().vendors.keys.sorted() },
            onIds = { ids, completion ->
                beginLoad(LoadingKeys.VENDORS_ENABLE)
                cmpManager.acceptVendors(ids) { result ->
                    endLoad(LoadingKeys.VENDORS_ENABLE)
                    completion(result)
                }
            }
        )

        ConsentMultiSelectRow(
            cmpManager = cmpManager,
            onLog = onLog,
            actionButtonText = "Disable",
            actionButtonColor = IosDemoPalette.red,
            isActionLoading = isLoading(LoadingKeys.VENDORS_DISABLE),
            selectPromptWhenEmpty = "Select vendors…",
            emptyDropdownMessage = "No vendors in current status.",
            emptySelectionLogMessage = "Disable vendors: select one or more vendors in the list first.",
            errorLogPrefix = "Disable vendors error",
            successMessage = { ids -> "Vendors disabled: ${ids.joinToString()}" },
            refreshIds = { m -> m.getUserStatus().vendors.keys.sorted() },
            onIds = { ids, completion ->
                beginLoad(LoadingKeys.VENDORS_DISABLE)
                cmpManager.rejectVendors(ids) { result ->
                    endLoad(LoadingKeys.VENDORS_DISABLE)
                    completion(result)
                }
            }
        )

        ConsentMultiSelectRow(
            cmpManager = cmpManager,
            onLog = onLog,
            actionButtonText = "Enable",
            actionButtonColor = IosDemoPalette.mint,
            isActionLoading = isLoading(LoadingKeys.PURPOSES_ENABLE),
            selectPromptWhenEmpty = "Select purposes…",
            emptyDropdownMessage = "No purposes in current status.",
            emptySelectionLogMessage = "Enable purposes: select one or more purposes in the list first.",
            errorLogPrefix = "Enable purposes error",
            successMessage = { ids -> "Purposes enabled: ${ids.joinToString()}" },
            refreshIds = { m -> m.getUserStatus().purposes.keys.sorted() },
            onIds = { ids, completion ->
                beginLoad(LoadingKeys.PURPOSES_ENABLE)
                cmpManager.acceptPurposes(ids, true) { result ->
                    endLoad(LoadingKeys.PURPOSES_ENABLE)
                    completion(result)
                }
            }
        )

        ConsentMultiSelectRow(
            cmpManager = cmpManager,
            onLog = onLog,
            actionButtonText = "Disable",
            actionButtonColor = IosDemoPalette.red,
            isActionLoading = isLoading(LoadingKeys.PURPOSES_DISABLE),
            selectPromptWhenEmpty = "Select purposes…",
            emptyDropdownMessage = "No purposes in current status.",
            emptySelectionLogMessage = "Disable purposes: select one or more purposes in the list first.",
            errorLogPrefix = "Disable purposes error",
            successMessage = { ids -> "Purposes disabled: ${ids.joinToString()}" },
            refreshIds = { m -> m.getUserStatus().purposes.keys.sorted() },
            onIds = { ids, completion ->
                beginLoad(LoadingKeys.PURPOSES_DISABLE)
                cmpManager.rejectPurposes(ids, true) { result ->
                    endLoad(LoadingKeys.PURPOSES_DISABLE)
                    completion(result)
                }
            }
        )

        DemoButton(
            text = "Reject All",
            containerColor = IosDemoPalette.red,
            isLoading = isLoading(LoadingKeys.REJECT_ALL),
            onClick = {
                beginLoad(LoadingKeys.REJECT_ALL)
                cmpManager.rejectAll { result ->
                    endLoad(LoadingKeys.REJECT_ALL)
                    result.onSuccess {
                        onLog("All consents rejected")
                    }.onFailure { error ->
                        onLog("Reject all error: ${error.message}")
                    }
                }
            }
        )

        DemoButton(
            text = "Accept All",
            containerColor = IosDemoPalette.green,
            isLoading = isLoading(LoadingKeys.ACCEPT_ALL),
            onClick = {
                beginLoad(LoadingKeys.ACCEPT_ALL)
                cmpManager.acceptAll { result ->
                    endLoad(LoadingKeys.ACCEPT_ALL)
                    result.onSuccess {
                        onLog("All consents accepted")
                    }.onFailure { error ->
                        onLog("Accept all error: ${error.message}")
                    }
                }
            }
        )

        DemoButton(
            text = "Open Consent Layer",
            containerColor = IosDemoPalette.indigo,
            isLoading = isLoading(LoadingKeys.FORCE_OPEN),
            onClick = {
                beginLoad(LoadingKeys.FORCE_OPEN)
                cmpManager.forceOpen() { result ->
                    endLoad(LoadingKeys.FORCE_OPEN)
                    result.onSuccess {
                        onLog("Open Consent Layer: success")
                    }.onFailure { error ->
                        onLog("Open Consent Layer error: ${error.message}")
                    }
                }
            }
        )

        DemoButton(
            text = "Jump to Settings Page",
            containerColor = IosDemoPalette.indigo,
            isLoading = isLoading(LoadingKeys.JUMP_SETTINGS),
            onClick = {
                beginLoad(LoadingKeys.JUMP_SETTINGS)
                cmpManager.forceOpen(jumpToSettings = true) { result ->
                    endLoad(LoadingKeys.JUMP_SETTINGS)
                    result.onSuccess {
                        onLog("Jump to Settings: success")
                    }.onFailure { error ->
                        onLog("Jump to Settings error: ${error.message}")
                    }
                }
            }
        )

        DemoButton(
            text = "Reset",
            containerColor = IosDemoPalette.black,
            onClick = {
                cmpManager.resetConsentManagementData()
                onLog("Consent data reset (local CMP data cleared)")
            }
        )

        DemoButton(
            text = "Import CMP String",
            containerColor = IosDemoPalette.teal,
            isLoading = isLoading(LoadingKeys.IMPORT_CMP),
            onClick = {
                beginLoad(LoadingKeys.IMPORT_CMP)
                cmpManager.importCMPInfo("Q1FMVW10Z1FMVW10Z0FmUTVDSVRCWUZnQUFBQUFBQUFBQWlnS3dOWF9HX19iWGx2LVg3MzZmdGtlWTFmOTloNzdzUXhCaGZKcy00RnpMdldfSndYMzJFek5FMzZ0cVlLbVJJQXUzVEJJUU50R0pqVVJWQ2hhb2dWcnpEc2FFeVVvVHRLSi1Ca2lITVJZMmRZQ0Z4dm00dGplUUNaNXZyXzkxZDUyUl90N2RyLTNkenl5NWhudjNhOV8tUzFXSmlkSzUtdEhfdjliUk9iLV9JLTlfeC1fNHY0X05fcEUyX2VUMXRfdFd2dDczOS04dHZfOV9fOTlfX19fZl9fX19fXzNfLV9mX19mX19fOEZYd0NURFFxSUF5d0pDUWcwRENDQkFDb0t3Z0lvRUFRQUFKQTBRRUFKZ3dLZGdZQUxyQ1JBQ0FGQUFNRUFJQUFRWkFBZ0FBQWdBUWlBQ0FBb0VBQUVBZ1VBQVlBRUF3RUFCQXdBQWdBc0JBSUFBUUhRTVV3SUlCQXNBRWpNaW9Vd0lRZ0VnZ0piS2hCSUFnUVZ3aENMUEFJZ0VSTUZBQUFBQUFVZ0FDQXNGZ2NTU0FsUWtFQVhFRzBBQUJBQWdFRUFCUWdrNU1BQVFCbXkxQjRNRzBaV21BWVBtQ1JEVEFNZ0NJSXlFZzBBQUEjXzUxXzUyXzUzXzU0XzU1XzU2XyNfczI4MTVfYzY0MDQzX3MyODE0X3MyNzYyX3MyODg1X3MyODE5X3MyODQ2X3MzMDM1X3MyNDM0X1VfIzEtLS0j") { result ->
                    endLoad(LoadingKeys.IMPORT_CMP)
                    result.onSuccess {
                        onLog("CMP string imported successfully")
                    }.onFailure { error ->
                        onLog("Import CMP string error: ${error.message}")
                    }
                }
            }
        )

        DemoButton(
            text = "Get Google Consent Mode Settings",
            containerColor = IosDemoPalette.indigo,
            onClick = {
                val settings = cmpManager.getGoogleConsentModeStatus()
                onLog(
                    buildString {
                        appendLine("Google Consent Mode Settings")
                        settings.forEach { (key, value) ->
                            appendLine("$key: $value")
                        }
                    }.trim()
                )
            }
        )

        DemoButton(
            text = "Inspect SharedPreferences",
            containerColor = IosDemoPalette.gray,
            onClick = {
                val prefs = context.getSharedPreferences(
                    "${context.packageName}_preferences",
                    Context.MODE_PRIVATE
                )
                val allEntries = prefs.all
                onLog(
                    buildString {
                        appendLine("SharedPreferences (default)")
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
                            appendLine("$key [$valueType]: $value")
                        }
                    }.trim()
                )
            }
        )
    }
}

@Composable
private fun ConsentMultiSelectRow(
    cmpManager: CMPManager,
    onLog: (String) -> Unit,
    actionButtonText: String,
    actionButtonColor: Color,
    isActionLoading: Boolean,
    selectPromptWhenEmpty: String,
    emptyDropdownMessage: String,
    emptySelectionLogMessage: String,
    errorLogPrefix: String,
    successMessage: (List<String>) -> String,
    refreshIds: (CMPManager) -> List<String>,
    onIds: (List<String>, (Result<Unit>) -> Unit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var availableIds by remember { mutableStateOf(listOf<String>()) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var lastActionClickMs by remember { mutableLongStateOf(0L) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = {
                    val ids = refreshIds(cmpManager)
                    availableIds = ids
                    selectedIds = selectedIds.intersect(ids.toSet())
                    menuExpanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = IosDemoPalette.titleIndigo
                )
            ) {
                val label = if (selectedIds.isEmpty()) {
                    selectPromptWhenEmpty
                } else {
                    "${selectedIds.size} selected"
                }
                Text(label)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                if (availableIds.isEmpty()) {
                    Text(
                        text = emptyDropdownMessage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    availableIds.forEach { id ->
                        val checked = id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds =
                                        if (checked) selectedIds - id
                                        else selectedIds + id
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = null
                            )
                            Text(
                                text = id,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                if (isActionLoading) return@Button
                val now = SystemClock.elapsedRealtime()
                if (now - lastActionClickMs < 400) return@Button
                lastActionClickMs = now
                if (selectedIds.isEmpty()) {
                    onLog(emptySelectionLogMessage)
                    return@Button
                }
                val ids = selectedIds.sorted()
                onIds(ids) { result ->
                    result.onSuccess {
                        onLog(successMessage(ids))
                    }.onFailure { error ->
                        onLog("$errorLogPrefix: ${error.message}")
                    }
                }
            },
            enabled = !isActionLoading,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = actionButtonColor,
                contentColor = IosDemoPalette.white
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp,
                        color = IosDemoPalette.white
                    )
                }
                Text(actionButtonText)
            }
        }
    }
}

@Composable
fun DemoButton(
    text: String,
    containerColor: Color,
    isLoading: Boolean = false,
    debounceMs: Long = 400,
    onClick: () -> Unit
) {
    var lastClickMs by remember { mutableLongStateOf(0L) }
    Button(
        onClick = {
            if (isLoading) return@Button
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickMs < debounceMs) return@Button
            lastClickMs = now
            onClick()
        },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = IosDemoPalette.white,
            disabledContainerColor = containerColor,
            disabledContentColor = IosDemoPalette.white
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                    strokeWidth = 2.dp,
                    color = IosDemoPalette.white
                )
            }
            Text(text)
        }
    }
}
