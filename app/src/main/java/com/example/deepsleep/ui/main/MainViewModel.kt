package com.example.deepsleep.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.DozeState
import com.example.deepsleep.model.Statistics
import com.example.deepsleep.root.DozeController
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.service.DeepSleepService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MainUiState(
    val hasRoot: Boolean = false,
    val isRootRequested: Boolean = false, // 是否已请求过授权
    val dozeState: DozeState = DozeState.UNKNOWN,
    val isServiceRunning: Boolean = false,
    val serviceRuntime: String = "未运行",
    val stats: Statistics = Statistics()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val statsRepository = StatsRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // 启动时请求 root 授权（仅一次）
        viewModelScope.launch {
            requestRootIfNeeded()
        }
        startStatusMonitor()
        loadStats()
    }

    // 请求 root 授权（如果尚未请求过）
    private suspend fun requestRootIfNeeded() {
        if (_uiState.value.isRootRequested) return
        _uiState.value = _uiState.value.copy(isRootRequested = true)

        val granted = RootCommander.requestRootAccess()
        _uiState.value = _uiState.value.copy(hasRoot = granted)
    }

    // 刷新 root 状态（例如在 onResume 中调用）
    suspend fun refreshRootStatus() {
        val hasRoot = RootCommander.checkRoot()
        _uiState.value = _uiState.value.copy(hasRoot = hasRoot)
    }

    // 检查当前 root 状态（直接从 uiState 获取，或实时检查）
    suspend fun hasRoot(): Boolean = RootCommander.checkRoot()

    private fun startStatusMonitor() {
        viewModelScope.launch {
            while (isActive) {
                val isRunning = DeepSleepService.isRunning
                val doze = if (isRunning) DozeController.getState() else DozeState.UNKNOWN
                _uiState.value = _uiState.value.copy(
                    isServiceRunning = isRunning,
                    dozeState = doze
                )
                updateRuntime()
                delay(2000)
            }
        }
    }

    private suspend fun updateRuntime() {
        val stats = statsRepository.loadStats()
        if (stats.serviceStartTime > 0) {
            val diff = System.currentTimeMillis() - stats.serviceStartTime
            val runtime = when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "${diff / 1000}秒"
                diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}分钟"
                else -> "${diff / TimeUnit.HOURS.toMillis(1)}小时${(diff % TimeUnit.HOURS.toMillis(1)) / TimeUnit.MINUTES.toMillis(1)}分钟"
            }
            _uiState.value = _uiState.value.copy(serviceRuntime = runtime)
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stats = statsRepository.loadStats())
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stats = statsRepository.loadStats())
        }
    }

    suspend fun forceEnterDeepSleep(): Boolean = DozeController.enterDeepSleep()

    suspend fun forceExitDeepSleep(): Boolean = DozeController.exitDeepSleep()
}