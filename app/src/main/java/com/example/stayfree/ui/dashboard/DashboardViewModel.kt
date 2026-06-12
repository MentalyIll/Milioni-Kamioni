package com.example.stayfree.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.BlockingRepository
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val blockingRepository: BlockingRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(TimeUtils.getTodayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val totalScreenTime: StateFlow<Long> = selectedDate
        .flatMapLatest { date -> usageRepository.getTotalScreenTimeForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalUnlocks: StateFlow<Int> = selectedDate
        .flatMapLatest { date -> usageRepository.getTotalUnlocksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topApps: StateFlow<List<AppUsage>> = selectedDate
        .flatMapLatest { date -> usageRepository.getUsageForDate(date) }
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBlockCount: StateFlow<Int> = blockingRepository.getActiveRules()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectToday() {
        viewModelScope.launch {
            val resetTime = prefs.dailyResetTimeMinutes.first()
            _selectedDate.value = TimeUtils.getEffectiveDate(resetTime)
        }
    }

    fun selectYesterday() {
        viewModelScope.launch {
            _selectedDate.value = TimeUtils.getDateStringDaysAgo(1)
        }
    }
}
