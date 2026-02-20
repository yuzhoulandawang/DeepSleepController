package com.example.deepsleep.ui.logs

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.LogEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 文件大小状态
    var fileSize by remember { mutableStateOf("") }
    LaunchedEffect(logs) {
        fileSize = try {
            viewModel.getLogSize()
        } catch (e: Exception) {
            "获取失败"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("运行日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshLogs() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val success = viewModel.clearLogs()
                                snackbarHostState.showSnackbar(
                                    if (success) "✅ 日志已清空" else "❌ 清空失败"
                                )
                                viewModel.refreshLogs()
                            }
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清空")
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val uri = viewModel.shareLogs(context)
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "分享日志")
                                    )
                                } else {
                                    snackbarHostState.showSnackbar("❌ 分享失败")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 日志统计信息
            LogStatsBar(
                totalLines = logs.size,
                fileSize = fileSize,
                modifier = Modifier.fillMaxWidth()
            )

            if (logs.isEmpty()) {
                EmptyLogView(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(logs, key = { it.hashCode() }) { logEntry ->
                        LogItem(entry = logEntry)
                    }
                }
            }
        }
    }
}

@Composable
fun LogStatsBar(totalLines: Int, fileSize: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("共 $totalLines 行", style = MaterialTheme.typography.labelMedium)
            Text("文件大小: $fileSize", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun LogItem(entry: LogEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.content.contains("==="))
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (entry.timestamp.isNotEmpty()) {
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = entry.content,
                style = if (entry.content.contains("==="))
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EmptyLogView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = "暂无日志",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}