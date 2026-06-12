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
class AddBlockRuleViewModel @Inject constructor(
    private val blockingRepository: BlockingRepository
) : ViewModel() {

    fun saveBlockNow(packageName: String, isPinLocked: Boolean) {
        viewModelScope.launch {
            blockingRepository.insertRule(
                BlockRuleEntity(
                    packageName = packageName,
                    blockType = BlockType.BLOCK_NOW.name,
                    isPinLocked = isPinLocked
                )
            )
        }
    }

    fun saveDailyLimit(packageName: String, limitMs: Long, isPinLocked: Boolean) {
        viewModelScope.launch {
            blockingRepository.insertRule(
                BlockRuleEntity(
                    packageName = packageName,
                    blockType = BlockType.DAILY_LIMIT.name,
                    dailyLimitMs = limitMs,
                    isPinLocked = isPinLocked
                )
            )
        }
    }

    fun saveSessionLimit(packageName: String, sessionMs: Long, breakMs: Long, isPinLocked: Boolean) {
        viewModelScope.launch {
            blockingRepository.insertRule(
                BlockRuleEntity(
                    packageName = packageName,
                    blockType = BlockType.SESSION.name,
                    sessionLimitMs = sessionMs,
                    sessionBreakMs = breakMs,
                    isPinLocked = isPinLocked
                )
            )
        }
    }

    fun saveSchedule(
        packageName: String,
        daysOfWeek: String,
        startMinutes: Int,
        endMinutes: Int,
        isPinLocked: Boolean
    ) {
        viewModelScope.launch {
            val ruleId = blockingRepository.insertRule(
                BlockRuleEntity(
                    packageName = packageName,
                    blockType = BlockType.SCHEDULED.name,
                    isPinLocked = isPinLocked
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
