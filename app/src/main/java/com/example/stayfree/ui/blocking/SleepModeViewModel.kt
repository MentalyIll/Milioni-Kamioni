package com.example.stayfree.ui.blocking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import com.example.stayfree.data.repository.BlockingRepository
import com.example.stayfree.domain.model.BlockType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepModeViewModel @Inject constructor(
    private val blockingRepository: BlockingRepository
) : ViewModel() {

    fun saveSleepMode(daysOfWeek: String, startMinutes: Int, endMinutes: Int) {
        viewModelScope.launch {
            // Deactivate previous sleep rules
            blockingRepository.deactivateAllForPackage("__sleep_mode__")

            val ruleId = blockingRepository.insertRule(
                BlockRuleEntity(
                    packageName = "__sleep_mode__",
                    blockType = BlockType.SLEEP.name,
                    isActive = true
                )
            )
            blockingRepository.insertSchedule(
                ScheduleEntity(
                    blockRuleId = ruleId,
                    daysOfWeek = daysOfWeek,
                    startTimeMinutes = startMinutes,
                    endTimeMinutes = endMinutes
                )
            )
        }
    }
}
