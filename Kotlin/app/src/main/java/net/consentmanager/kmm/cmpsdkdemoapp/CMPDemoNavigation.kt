package net.consentmanager.kmm.cmpsdkdemoapp

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.consentmanager.cm_sdk_android_v3.CMPManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

@Composable
fun CMPDemoWithNav(cmpManager: CMPManager) {
    var tab by remember { mutableIntStateOf(0) }
    val logLines = remember { mutableStateListOf<String>() }
    fun appendLog(message: String) {
        logLines.add("[${formatTimestamp()}] ${message.trim()}")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Menu, contentDescription = null) },
                    label = { Text("Logs") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when (tab) {
                0 -> CMPDemoScreen(cmpManager, onLog = ::appendLog)
                1 -> LogScreen(logLines = logLines)
            }
        }
    }
}

/**
 * Draws a vertical scrollbar thumb on the end edge for [ScrollState] (Android foundation has no
 * [androidx.compose.foundation.VerticalScrollbar] like desktop).
 */
private fun Modifier.verticalScrollbarThumb(scrollState: ScrollState): Modifier = composed {
    val density = LocalDensity.current
    val trackPx = with(density) { 4.dp.toPx() }
    val minThumbPx = with(density) { 24.dp.toPx() }
    val thumbColor = Color.Gray.copy(alpha = 0.45f)
    drawWithContent {
        drawContent()
        val maxScroll = scrollState.maxValue
        if (maxScroll <= 0) return@drawWithContent
        val viewportH = size.height
        val contentH = viewportH + maxScroll
        val thumbH = (viewportH * viewportH / contentH).coerceIn(minThumbPx, viewportH)
        val maxThumbTravel = viewportH - thumbH
        if (maxThumbTravel <= 0f) return@drawWithContent
        val thumbY = (scrollState.value.toFloat() / maxScroll) * maxThumbTravel
        val x = size.width - trackPx
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(x, thumbY),
            size = Size(trackPx, thumbH),
            cornerRadius = CornerRadius(trackPx / 2, trackPx / 2)
        )
    }
}

@Composable
private fun LogScreen(logLines: List<String>) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val bottomEpsilonPx = remember(density) { with(density) { 8.dp.toPx() } }
    val scope = rememberCoroutineScope()

    LaunchedEffect(logLines.size, logLines.lastOrNull()) {
        if (logLines.isEmpty()) return@LaunchedEffect
        val v = scrollState.value
        val max = scrollState.maxValue
        val wasAtBottom = max == 0 || v >= max - bottomEpsilonPx
        if (!wasAtBottom) return@LaunchedEffect
        withFrameNanos { }
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val maxScroll = scrollState.maxValue
    val showScrollToBottom =
        maxScroll > 0 && scrollState.value < maxScroll - bottomEpsilonPx

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IosDemoPalette.screenBackground)
            .padding(16.dp)
    ) {
        if (logLines.isEmpty()) {
            Text(
                text = "No messages yet. Use actions on Home to append output here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .verticalScrollbarThumb(scrollState)
                    ) {
                        Text(
                            text = logLines.joinToString("\n\n"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (showScrollToBottom) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        content = {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Scroll to latest log"
                            )
                        }
                    )
                }
            }
        }
    }
}
