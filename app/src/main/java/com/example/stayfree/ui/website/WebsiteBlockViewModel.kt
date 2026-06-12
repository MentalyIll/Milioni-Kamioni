package com.example.stayfree.ui.website

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.entity.WebsiteBlockEntity
import com.example.stayfree.data.repository.WebsiteBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteBlockViewModel @Inject constructor(
    private val repository: WebsiteBlockRepository
) : ViewModel() {

    val websites: StateFlow<List<WebsiteBlockEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWebsite(domain: String, dailyCapMs: Long?) {
        viewModelScope.launch {
            repository.insert(WebsiteBlockEntity(domain = domain, dailyCapMs = dailyCapMs))
        }
    }

    fun toggleWebsite(entity: WebsiteBlockEntity) {
        viewModelScope.launch {
            repository.update(entity.copy(isActive = !entity.isActive))
        }
    }

    fun deleteWebsite(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
