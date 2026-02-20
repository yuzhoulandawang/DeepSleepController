package com.example.deepsleep.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.DozeState
import com.example.deepsleep.service.DeepSleepService
import com.example.deepsleep.ui.theme.DeepSleepPurple
import com.example.deepsleep.ui.theme.InfoBlue
import com.example.deepsleep.ui.theme.SuccessGreen
import com.example.deepsleep.ui.theme.WarningYellow
import com.example.deepsleep.ui.theme.ErrorRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToWhitelist: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("深度睡眠控制器") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Root 状态
            RootStatusCard(hasRoot = uiState.hasRoot)

            // 服务状态和 Doze 状态
            ServiceStatusCard(
                isRunning = uiState.isServiceRunning,
                dozeState = uiState.dozeState,
                runtime = uiState.serviceRuntime
            )

            // 统计卡片
            StatsCard(stats = uiState.stats)

            // 控制按钮
            ControlButtons(
                isServiceRunning = uiState.isServiceRunning,
                hasRoot = uiState.hasRoot,
                onStartService = {
                    scope.launch {
                        if (viewModel.hasRoot()) {
                            context.startService(
                                Intent(context, DeepSleepService::class.java).apply {
                                    action = DeepSleepService.ACTION_START
                                }
                            )
                            snackbarHostState.showSnackbar("✅ 服务已启动")
                        } else {
                            snackbarHostState.showSnackbar("❌ 无法获取 Root 权限，请使用 Magisk 授权")
                        }
                    }
                },
                onStopService = {
                    scope.launch {   // 修复：添加协程作用域
                        context.startService(
                            Intent(context, DeepSleepService::class.java).apply {
                                action = DeepSleepService.ACTION_STOP
                            }
                        )
                        snackbarHostState.showSnackbar("⏹️ 服务已停止")
                    }
                },
                onForceEnter = {
                    scope.launch {
                        val success = viewModel.forceEnterDeepSleep()
                        snackbarHostState.showSnackbar(
                            if (success) "✅ 已进入深度睡眠" else "❌ 进入失败"
                        )
                    }
                },
                onForceExit = {
                    scope.launch {
                        val success = viewModel.forceExitDeepSleep()
                        snackbarHostState.showSnackbar(
                            if (success) "✅ 已退出深度睡眠" else "❌ 退出失败"
                        )
                    }
                }
            )

            // 功能入口
            FeatureRow(
                onLogsClick = onNavigateToLogs,
                onWhitelistClick = onNavigateToWhitelist
            )
        }
    }
}

// 以下辅助组件保持不变
@Composable
fun RootStatusCard(hasRoot: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasRoot) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasRoot) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (hasRoot) SuccessGreen else ErrorRed
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (hasRoot) "Root 权限已获取" else "Root 权限未获取",
                color = if (hasRoot) 
                    MaterialTheme.colorScheme.onTertiaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ServiceStatusCard(isRunning: Boolean, dozeState: DozeState, runtime: String) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("服务状态", style = MaterialTheme.typography.titleMedium)
                if (isRunning) {
                    Badge(containerColor = SuccessGreen) { Text("运行中") }
                } else {
                    Badge(containerColor = MaterialTheme.colorScheme.error) { Text("已停止") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            StatusRow("Doze 状态", dozeState.displayName)
            StatusRow("运行时长", runtime)
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatsCard(stats: com.example.deepsleep.model.Statistics) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("统计概览", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("进入次数", stats.totalEnterCount.toString())
                StatItem("成功率", 
                    if (stats.totalEnterCount > 0) 
                        "${stats.totalEnterSuccess * 100 / stats.totalEnterCount}%" 
                    else "0%"
                )
                StatItem("修复率",
                    if (stats.totalAutoExitCount > 0)
                        "${stats.totalAutoExitRecover * 100 / stats.totalAutoExitCount}%"
                    else "0%"
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ControlButtons(
    isServiceRunning: Boolean,
    hasRoot: Boolean,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onForceEnter: () -> Unit,
    onForceExit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartService,
                enabled = !isServiceRunning && hasRoot,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(4.dp))
                Text("启动")
            }
            OutlinedButton(
                onClick = onStopService,
                enabled = isServiceRunning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(4.dp))
                Text("停止")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onForceEnter,
                enabled = hasRoot,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Bedtime, null)
                Spacer(Modifier.width(4.dp))
                Text("强制进入")
            }
            OutlinedButton(
                onClick = onForceExit,
                enabled = hasRoot,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.WbSunny, null)
                Spacer(Modifier.width(4.dp))
                Text("强制退出")
            }
        }
    }
}

@Composable
fun FeatureRow(onLogsClick: () -> Unit, onWhitelistClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onLogsClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Description, null)
            Spacer(Modifier.width(4.dp))
            Text("日志")
        }
        OutlinedButton(
            onClick = onWhitelistClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PlaylistAdd, null)
            Spacer(Modifier.width(4.dp))
            Text("白名单")
        }
    }
}