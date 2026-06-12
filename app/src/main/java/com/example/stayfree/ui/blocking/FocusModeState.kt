package com.example.stayfree.ui.blocking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FocusModeSnapshot(
    val active: Boolean,
    val endTime: Long,
    val whitelist: Set<String>,
    val isWhitelist: Boolean
)

object FocusModeState {
    private val _state = MutableStateFlow(FocusModeSnapshot(false, 0L, emptySet(), true))
    val state: StateFlow<FocusModeSnapshot> = _state.asStateFlow()

    fun update(active: Boolean, endTime: Long, whitelist: Set<String>, isWhitelist: Boolean) {
        _state.value = FocusModeSnapshot(active, endTime, whitelist, isWhitelist)
    }
}
