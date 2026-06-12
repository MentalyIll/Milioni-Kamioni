package com.example.stayfree.ui.blocking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.repository.BlockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockingViewModel @Inject constructor(
    private val blockingRepository: BlockingRepository
) : ViewModel() {

    val allRules: StateFlow<List<BlockRuleEntity>> = blockingRepository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleRule(id: Long, active: Boolean) {
        viewModelScope.launch { blockingRepository.setRuleActive(id, active) }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch { blockingRepository.deleteRule(id) }
    }
}
