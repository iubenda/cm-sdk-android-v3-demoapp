package net.consentmanager.kmm.cmpsdkdemoapp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.consentmanager.cm_sdk_android_v3.CMPManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_LOG_LINES = 3_000
private const val DEST_HOME = 0
private const val DEST_LOGS = 1
private const val DEST_DEBUG = 2

private fun formatTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMPDemoWithNav(cmpManager: CMPManager) {
    var destination by remember { mutableIntStateOf(DEST_HOME) }
    var previousTab by remember { mutableIntStateOf(DEST_HOME) }
    var menuExpanded by remember { mutableStateOf(false) }
    var logsHighlightGeneration by remember { mutableIntStateOf(0) }
    val logLines = remember { mutableStateListOf<String>() }
    fun appendLog(message: String) {
        logLines.add("[${formatTimestamp()}] ${message.trim()}")
        while (logLines.size > MAX_LOG_LINES) {
            logLines.removeAt(0)
        }
    }
    fun onOperationSuccess() {
        logsHighlightGeneration++
    }

    val showingDebug = destination == DEST_DEBUG

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (destination) {
                            DEST_DEBUG -> stringResource(R.string.debug_title)
                            DEST_LOGS -> stringResource(R.string.nav_logs)
                            else -> stringResource(R.string.nav_home)
                        }
                    )
                },
                navigationIcon = {
                    if (showingDebug) {
                        IconButton(onClick = { destination = previousTab }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(
                                    R.string.debug_back_content_description
                                )
                            )
                        }
                    } else {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(
                                        R.string.menu_content_description
                                    )
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_debug)) },
                                    onClick = {
                                        menuExpanded = false
                                        previousTab = destination
                                        destination = DEST_DEBUG
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!showingDebug) {
                NavigationBar {
                    // Icons use contentDescription = null because visible labels supply TalkBack semantics (M3).
                    NavigationBarItem(
                        selected = destination == DEST_HOME,
                        onClick = { destination = DEST_HOME },
                        icon = {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = destination == DEST_LOGS,
                        onClick = { destination = DEST_LOGS },
                        icon = {
                            LogsNavIconWithGlow(highlightGeneration = logsHighlightGeneration)
                        },
                        label = { Text(stringResource(R.string.nav_logs)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (destination) {
                DEST_HOME -> CMPDemoScreen(
                    cmpManager = cmpManager,
                    onLog = ::appendLog,
                    onOperationSuccess = ::onOperationSuccess
                )
                DEST_LOGS -> LogScreen(logLines = logLines)
                DEST_DEBUG -> DebugScreen(
                    cmpManager = cmpManager,
                    onLog = ::appendLog,
                    onOperationSuccess = ::onOperationSuccess
                )
            }
        }
    }
}

@Composable
private fun LogsNavIconWithGlow(highlightGeneration: Int) {
    val glowProgress = remember { Animatable(0f) }

    LaunchedEffect(highlightGeneration) {
        if (highlightGeneration <= 0) return@LaunchedEffect
        glowProgress.snapTo(0f)
        repeat(2) {
            glowProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            glowProgress.animateTo(
                targetValue = 0.15f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        glowProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    val glow = glowProgress.value
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp)
    ) {
        if (glow > 0.01f) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scale(1f + glow * 0.45f)
                    .background(
                        color = IosDemoPalette.indigo.copy(alpha = glow * 0.35f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(1f + glow * 0.2f)
                    .border(
                        width = (1.5f + glow * 2f).dp,
                        color = IosDemoPalette.indigo.copy(alpha = 0.25f + glow * 0.75f),
                        shape = CircleShape
                    )
            )
        }
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = null
        )
    }
}

@Composable
private fun LogScreen(logLines: List<String>) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(logLines.size, logLines.lastOrNull()) {
        if (logLines.isEmpty()) return@LaunchedEffect
        withFrameNanos { }
        if (!listState.canScrollForward) {
            listState.scrollToItem(logLines.lastIndex)
        }
    }

    val showScrollToBottom = listState.canScrollForward

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(logLines.size) { index ->
                            Text(
                                text = logLines[index],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (showScrollToBottom) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.scrollToItem(logLines.lastIndex)
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
