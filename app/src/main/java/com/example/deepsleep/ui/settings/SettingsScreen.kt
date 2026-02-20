package com.example.deepsleep.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(repository)
    )
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 进程压制
            SettingsSection(title = "进程压制") {
                SwitchSetting(
                    title = "启用进程压制",
                    checked = settings.suppressEnabled,
                    onCheckedChange = { viewModel.setSuppressEnabled(it) }
                )
                
                if (settings.suppressEnabled) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "压制模式",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = settings.suppressMode == "conservative",
                            onClick = { viewModel.setSuppressMode("conservative") },
                            label = { Text("保守") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = settings.suppressMode == "aggressive",
                            onClick = { viewModel.setSuppressMode("aggressive") },
                            label = { Text("激进") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = "保守：仅息屏时压制；激进：始终压制",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // 后台优化
            SettingsSection(title = "后台优化") {
                SwitchSetting(
                    title = "启动时自动优化第三方应用",
                    subtitle = "限制 RUN_ANY_IN_BACKGROUND 和 WAKE_LOCK",
                    checked = settings.backgroundOptimizationEnabled,
                    onCheckedChange = { viewModel.setBackgroundOptimizationEnabled(it) }
                )
            }
            
            // 防抖间隔（简化后固定，不提供调节）
            SettingsSection(title = "其他") {
                Text(
                    text = "防抖间隔：3秒（固定）",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "压制间隔：60秒（固定）",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "OOM 值：800（固定）",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
