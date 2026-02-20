package com.example.deepsleep.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    fun setSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSuppressEnabled(enabled)
        }
    }
    
    fun setSuppressMode(mode: String) {
        viewModelScope.launch {
            repository.setSuppressMode(mode)
        }
    }
    
    fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBackgroundOptimizationEnabled(enabled)
        }
    }
    
    companion object {
        fun provideFactory(repository: SettingsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SettingsViewModel(repository) }
            }
    }
}
