package com.example.deepsleep.ui.logs

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogsViewModel : ViewModel() {
    
    private val repository = LogRepository()
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    init {
        refreshLogs()
    }
    
    fun refreshLogs() {
        viewModelScope.launch {
            _logs.value = repository.readLogs()
        }
    }
    
    suspend fun clearLogs(): Boolean = repository.clearLogs()
    
    fun getLogSize(): String = repository.getLogSize()
    
    suspend fun shareLogs(context: Context): Uri? = repository.createShareableFile(context)
}
