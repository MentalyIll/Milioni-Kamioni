package com.example.stayfree.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatsPeriod { DAILY, WEEKLY, MONTHLY }

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.DAILY)
    val period: StateFlow<StatsPeriod> = _period.asStateFlow()

    val appUsageList: StateFlow<List<AppUsage>> = _period.flatMapLatest { period ->
        val fromDate = when (period) {
            StatsPeriod.DAILY -> TimeUtils.getTodayString()
            StatsPeriod.WEEKLY -> TimeUtils.getDateStringDaysAgo(7)
            StatsPeriod.MONTHLY -> TimeUtils.getDateStringDaysAgo(30)
        }
        usageRepository.getUsageForDate(fromDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalScreenTime: StateFlow<Long> = _period.flatMapLatest { period ->
        val date = when (period) {
            StatsPeriod.DAILY -> TimeUtils.getTodayString()
            StatsPeriod.WEEKLY -> TimeUtils.getDateStringDaysAgo(7)
            StatsPeriod.MONTHLY -> TimeUtils.getDateStringDaysAgo(30)
        }
        usageRepository.getTotalScreenTimeForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalUnlocks: StateFlow<Int> = _period.flatMapLatest { period ->
        val date = when (period) {
            StatsPeriod.DAILY -> TimeUtils.getTodayString()
            StatsPeriod.WEEKLY -> TimeUtils.getDateStringDaysAgo(7)
            StatsPeriod.MONTHLY -> TimeUtils.getDateStringDaysAgo(30)
        }
        usageRepository.getTotalUnlocksForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setPeriod(p: StatsPeriod) { _period.value = p }
}
