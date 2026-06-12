package com.example.stayfree.domain.model

data class BlockRule(
    val id: Long,
    val packageName: String,
    val appName: String,
    val blockType: BlockType,
    val isActive: Boolean,
    val isPinLocked: Boolean,
    val dailyLimitMs: Long?,
    val sessionLimitMs: Long?,
    val sessionBreakMs: Long?,
    val schedules: List<Schedule> = emptyList(),
    val syncId: String
)
