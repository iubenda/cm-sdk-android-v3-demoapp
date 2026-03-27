package net.consentmanager.kmm.cmpsdkdemoapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private val LANGUAGES = listOf("EN", "IT", "DE", "FR", "ES", "PT", "NL", "PL")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    configuration: CMPConfiguration,
    onConfigurationChange: (CMPConfiguration) -> Unit,
    onLoadCMP: () -> Unit
) {
    var showValidationAlert by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var positionExpanded by remember { mutableStateOf(false) }
    var backgroundExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CMP Configuration") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CMP Settings", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = configuration.codeId,
                        onValueChange = { onConfigurationChange(configuration.copy(codeId = it)) },
                        label = { Text("Code-ID") },
                        modifier = Modifier.fillMaxWidth().testTag("Code-ID"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = configuration.language,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Language") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .testTag("Language")
                        )
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        onConfigurationChange(configuration.copy(language = lang))
                                        languageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = configuration.appName,
                        onValueChange = { onConfigurationChange(configuration.copy(appName = it)) },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth().testTag("App Name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = configuration.domain,
                        onValueChange = { onConfigurationChange(configuration.copy(domain = it)) },
                        label = { Text("Domain") },
                        modifier = Modifier.fillMaxWidth().testTag("Domain"),
                        singleLine = true
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Webview Appearance", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = positionExpanded,
                        onExpandedChange = { positionExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = configuration.position.label,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Position") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = positionExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .testTag("Position")
                        )
                        DropdownMenu(
                            expanded = positionExpanded,
                            onDismissRequest = { positionExpanded = false }
                        ) {
                            WebviewPosition.entries.forEach { pos ->
                                DropdownMenuItem(
                                    text = { Text(pos.label) },
                                    onClick = {
                                        onConfigurationChange(configuration.copy(position = pos))
                                        positionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = backgroundExpanded,
                        onExpandedChange = { backgroundExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = configuration.backgroundStyle.label,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Background") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = backgroundExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .testTag("Background")
                        )
                        DropdownMenu(
                            expanded = backgroundExpanded,
                            onDismissRequest = { backgroundExpanded = false }
                        ) {
                            WebviewBackgroundStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.label) },
                                    onClick = {
                                        onConfigurationChange(configuration.copy(backgroundStyle = style))
                                        backgroundExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Corner Radius: ${configuration.cornerRadius.toInt()}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = configuration.cornerRadius,
                        onValueChange = { onConfigurationChange(configuration.copy(cornerRadius = it)) },
                        valueRange = 0f..30f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth().testTag("Corner Radius")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Respects Safe Area", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = configuration.respectsSafeArea,
                            onCheckedChange = { onConfigurationChange(configuration.copy(respectsSafeArea = it)) },
                            modifier = Modifier.testTag("Respects Safe Area")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allows Orientation Changes", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = configuration.allowsOrientationChanges,
                            onCheckedChange = { onConfigurationChange(configuration.copy(allowsOrientationChanges = it)) },
                            modifier = Modifier.testTag("Allows Orientation Changes")
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (configuration.isValid) {
                        onLoadCMP()
                    } else {
                        showValidationAlert = true
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("Load CMP")
            ) {
                Text("Load CMP")
            }
        }
    }

    if (showValidationAlert) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Invalid Configuration") },
            text = { Text(configuration.validationError ?: "") },
            confirmButton = {
                Button(onClick = { }) { Text("OK") }
            }
        )
    }
}
