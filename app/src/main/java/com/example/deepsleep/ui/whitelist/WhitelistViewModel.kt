package com.example.deepsleep.ui.whitelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.WhitelistRepository
import com.example.deepsleep.model.WhitelistItem
import com.example.deepsleep.model.WhitelistType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WhitelistUiState(
    val currentType: WhitelistType = WhitelistType.SUPPRESS,
    val items: List<WhitelistItem> = emptyList()
)

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WhitelistRepository()
    
    private val _uiState = MutableStateFlow(WhitelistUiState())
    val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()
    
    init {
        loadItems()
    }
    
    fun switchType(type: WhitelistType) {
        _uiState.value = _uiState.value.copy(currentType = type)
        loadItems()
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            val items = repository.loadItems(
                getApplication(),
                _uiState.value.currentType
            )
            _uiState.value = _uiState.value.copy(items = items)
        }
    }
    
    suspend fun addItem(name: String, note: String, type: WhitelistType) {
        repository.addItem(getApplication(), name, note, type)
        loadItems()
    }
    
    suspend fun updateItem(item: WhitelistItem) {
        repository.updateItem(getApplication(), item)
        loadItems()
    }
    
    suspend fun deleteItem(item: WhitelistItem) {
        repository.deleteItem(getApplication(), item)
        loadItems()
    }
}
