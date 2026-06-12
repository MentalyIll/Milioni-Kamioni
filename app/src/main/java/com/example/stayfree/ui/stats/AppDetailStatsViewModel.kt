package com.example.stayfree.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AppDetailStatsViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _packageName = MutableStateFlow("")

    val todayUsage: StateFlow<Long> = _packageName.flatMapLatest { pkg ->
        if (pkg.isEmpty()) flowOf(0L)
        else usageRepository.getScreenTimeForPackageOnDate(pkg, TimeUtils.getTodayString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayUnlocks: StateFlow<Int> = _packageName.flatMapLatest { pkg ->
        if (pkg.isEmpty()) flowOf(0)
        else usageRepository.getUnlocksForPackageOnDate(pkg, TimeUtils.getTodayString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklyUsage: StateFlow<List<AppUsage>> = _packageName.flatMapLatest { pkg ->
        if (pkg.isEmpty()) flowOf(emptyList())
        else usageRepository.getUsageForPackage(pkg, TimeUtils.getDateStringDaysAgo(7))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadApp(packageName: String) {
        _packageName.value = packageName
    }
}
