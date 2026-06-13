package com.example.stayfree.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.example.stayfree.data.local.db.dao.BlockRuleDao
import com.example.stayfree.data.local.db.dao.ScheduleDao
import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import com.example.stayfree.domain.BlockRuleEvaluator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockingRepositoryImpl @Inject constructor(
    private val blockRuleDao: BlockRuleDao,
    private val scheduleDao: ScheduleDao,
    @ApplicationContext private val context: Context
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

    /**
     * Replaces the PACKAGE_REMOVED broadcast receiver (implicit broadcasts are
     * not delivered to manifest receivers on API 26+). Called on app start.
     */
    override suspend fun deactivateRulesForUninstalledApps() {
        val pm = context.packageManager
        blockRuleDao.getActiveRulesOnce()
            .map { it.packageName }
            .distinct()
            .filter { it != BlockRuleEvaluator.SLEEP_MODE_PACKAGE }
            .forEach { pkg ->
                val installed = try {
                    pm.getApplicationInfo(pkg, 0)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
                if (!installed) blockRuleDao.deactivateAllForPackage(pkg)
            }
    }
}
