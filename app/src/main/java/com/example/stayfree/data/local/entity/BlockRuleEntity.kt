package com.example.stayfree.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "block_rules")
data class BlockRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val blockType: String,          // BLOCK_NOW | SCHEDULED | DAILY_LIMIT | SESSION | FOCUS | SLEEP
    val isActive: Boolean = true,
    val isPinLocked: Boolean = false,
    val dailyLimitMs: Long? = null,
    val sessionLimitMs: Long? = null,
    val sessionBreakMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncId: String = UUID.randomUUID().toString()
)
