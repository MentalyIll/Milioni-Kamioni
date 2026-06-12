package com.example.stayfree.ui.blocking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusModeViewModel @Inject constructor() : ViewModel() {

    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive: StateFlow<Boolean> = _isFocusActive.asStateFlow()

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private var durationMinutes = 25
    private var isWhitelistMode = true
    private var countdownJob: Job? = null

    fun setDurationMinutes(minutes: Int) { durationMinutes = minutes }
    fun setWhitelistMode(whitelist: Boolean) { isWhitelistMode = whitelist }

    fun startFocusMode() {
        val durationMs = durationMinutes * 60_000L
        val endTime = System.currentTimeMillis() + durationMs
        _remainingMs.value = durationMs
        _isFocusActive.value = true

        FocusModeState.update(
            active = true,
            endTime = endTime,
            whitelist = emptySet(),
            isWhitelist = isWhitelistMode
        )

        countdownJob = viewModelScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1000L
                _remainingMs.value = remaining
            }
            stopFocusMode()
        }
    }

    fun stopFocusMode() {
        countdownJob?.cancel()
        _isFocusActive.value = false
        _remainingMs.value = 0L
        FocusModeState.update(active = false, endTime = 0L, whitelist = emptySet(), isWhitelist = true)
    }
}
