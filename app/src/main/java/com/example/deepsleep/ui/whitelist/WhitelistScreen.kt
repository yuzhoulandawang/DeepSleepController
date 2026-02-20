package com.example.deepsleep.ui.whitelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.WhitelistItem
import com.example.deepsleep.model.WhitelistType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(
    viewModel: WhitelistViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WhitelistItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("白名单管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 类型切换
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { expanded = true },
                            label = { 
                                Text(
                                    when (uiState.currentType) {
                                        WhitelistType.SUPPRESS -> "进程压制"
                                        WhitelistType.BACKGROUND -> "后台优化"
                                    }
                                ) 
                            },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("进程压制") },
                                onClick = {
                                    viewModel.switchType(WhitelistType.SUPPRESS)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("后台优化") },
                                onClick = {
                                    viewModel.switchType(WhitelistType.BACKGROUND)
                                    expanded = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
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
            InfoBanner(type = uiState.currentType)
            
            if (uiState.items.isEmpty()) {
                EmptyWhitelistView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        WhitelistItemCard(
                            item = item,
                            onEdit = { editingItem = item },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteItem(item)
                                    snackbarHostState.showSnackbar("已删除 ${item.name}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog || editingItem != null) {
        AddEditDialog(
            item = editingItem,
            type = uiState.currentType,
            onDismiss = { 
                showAddDialog = false
                editingItem = null
            },
            onConfirm = { name, note ->
                scope.launch {
                    if (editingItem != null) {
                        viewModel.updateItem(editingItem!!.copy(name = name, note = note))
                        snackbarHostState.showSnackbar("已更新")
                    } else {
                        viewModel.addItem(name, note, uiState.currentType)
                        snackbarHostState.showSnackbar("已添加 $name")
                    }
                    showAddDialog = false
                    editingItem = null
                }
            }
        )
    }
}

@Composable
fun InfoBanner(type: WhitelistType) {
    val (icon, title, desc) = when (type) {
        WhitelistType.SUPPRESS -> Triple(
            Icons.Default.Security,
            "进程压制白名单",
            "这些进程不会被调整 OOM 分数"
        )
        WhitelistType.BACKGROUND -> Triple(
            Icons.Default.AppShortcut,
            "后台优化白名单",
            "这些应用不会被限制后台权限"
        )
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WhitelistItemCard(
    item: WhitelistItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyWhitelistView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = "白名单为空",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击右上角 + 添加条目",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AddEditDialog(
    item: WhitelistItem?,
    type: WhitelistType,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var nameError by remember { mutableStateOf(false) }
    
    val title = if (item == null) "添加白名单" else "编辑白名单"
    val confirmText = if (item == null) "添加" else "保存"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = false
                    },
                    label = { 
                        Text(
                            when (type) {
                                WhitelistType.SUPPRESS -> "进程名/关键字"
                                WhitelistType.BACKGROUND -> "应用包名"
                            }
                        ) 
                    },
                    placeholder = { 
                        Text(
                            when (type) {
                                WhitelistType.SUPPRESS -> "com.android.systemui"
                                WhitelistType.BACKGROUND -> "com.tencent.mm"
                            }
                        ) 
                    },
                    isError = nameError,
                    supportingText = if (nameError) { 
                        { Text("名称不能为空") } 
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    placeholder = { Text("用于识别用途") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(name.trim(), note.trim())
                    }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
