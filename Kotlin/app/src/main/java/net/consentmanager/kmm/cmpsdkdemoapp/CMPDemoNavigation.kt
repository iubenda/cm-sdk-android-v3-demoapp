package net.consentmanager.kmm.cmpsdkdemoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.consentmanager.cm_sdk_android_v3.CMPManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_LOG_LINES = 3_000

private fun formatTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

@Composable
fun CMPDemoWithNav(cmpManager: CMPManager) {
    var tab by remember { mutableIntStateOf(0) }
    val logLines = remember { mutableStateListOf<String>() }
    fun appendLog(message: String) {
        logLines.add("[${formatTimestamp()}] ${message.trim()}")
        while (logLines.size > MAX_LOG_LINES) {
            logLines.removeAt(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                // Icons use contentDescription = null because visible labels supply TalkBack semantics (M3).
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.nav_logs)) }
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
