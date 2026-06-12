package com.example.stayfree.data.repository

import com.example.stayfree.data.local.db.dao.BlockRuleDao
import com.example.stayfree.data.local.db.dao.ScheduleDao
import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockingRepositoryImpl @Inject constructor(
    private val blockRuleDao: BlockRuleDao,
    private val scheduleDao: ScheduleDao
) : BlockingRepository {

    override fun getAllRules(): Flow<List<BlockRuleEntity>> = blockRuleDao.getAllRules()
    override fun getActiveRules(): Flow<List<BlockRuleEntity>> = blockRuleDao.getActiveRules()
    override suspend fun getActiveRulesOnce(): List<BlockRuleEntity> = blockRuleDao.getActiveRulesOnce()
    override suspend fun getActiveRulesForPackage(pkg: String): List<BlockRuleEntity> = blockRuleDao.getActiveRulesForPackage(pkg)
    override suspend fun getSchedulesForRule(ruleId: Long): List<ScheduleEntity> = scheduleDao.getSchedulesForRuleOnce(ruleId)
    override suspend fun insertRule(rule: BlockRuleEntity): Long = blockRuleDao.insert(rule)
    override suspend fun insertSchedule(schedule: ScheduleEntity) = scheduleDao.insert(schedule).let { }
    override suspend fun updateRule(rule: BlockRuleEntity) = blockRuleDao.update(rule)
    override suspend fun deleteRule(id: Long) = blockRuleDao.deleteById(id)
    override suspend fun setRuleActive(id: Long, active: Boolean) = blockRuleDao.setActive(id, active)
    override suspend fun deactivateAllForPackage(pkg: String) = blockRuleDao.deactivateAllForPackage(pkg)
}
