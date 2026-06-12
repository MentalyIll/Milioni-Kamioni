package com.example.stayfree.data.repository

import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

interface BlockingRepository {
    fun getAllRules(): Flow<List<BlockRuleEntity>>
    fun getActiveRules(): Flow<List<BlockRuleEntity>>
    suspend fun getActiveRulesOnce(): List<BlockRuleEntity>
    suspend fun getActiveRulesForPackage(pkg: String): List<BlockRuleEntity>
    suspend fun getSchedulesForRule(ruleId: Long): List<ScheduleEntity>
    suspend fun insertRule(rule: BlockRuleEntity): Long
    suspend fun insertSchedule(schedule: ScheduleEntity)
    suspend fun updateRule(rule: BlockRuleEntity)
    suspend fun deleteRule(id: Long)
    suspend fun setRuleActive(id: Long, active: Boolean)
    suspend fun deactivateAllForPackage(pkg: String)
}
