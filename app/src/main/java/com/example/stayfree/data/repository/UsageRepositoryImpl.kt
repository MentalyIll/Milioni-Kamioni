package com.example.stayfree.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.stayfree.data.local.db.dao.AppUsageDao
import com.example.stayfree.data.local.entity.AppUsageEntity
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.AppInfoUtils
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val dao: AppUsageDao,
    private val usageStatsManager: UsageStatsManager,
    @ApplicationContext private val context: Context
) : UsageRepository {

    override fun getUsageForDate(date: String): Flow<List<AppUsage>> =
        dao.getUsageForDate(date).map { list -> list.map { it.toDomain() } }

    override fun getTotalScreenTimeForDate(date: String): Flow<Long> =
        dao.getTotalScreenTimeForDate(date).map { it ?: 0L }

    override fun getTotalUnlocksForDate(date: String): Flow<Int> =
        dao.getTotalUnlocksForDate(date).map { it ?: 0 }

    override fun getUsageForPackage(packageName: String, fromDate: String): Flow<List<AppUsage>> =
        dao.getUsageForPackage(packageName, fromDate).map { list -> list.map { it.toDomain() } }

    override fun getScreenTimeForPackageOnDate(packageName: String, date: String): Flow<Long> =
        dao.getUsageForPackageOnDate(packageName, date).map { it?.totalTimeMs ?: 0L }

    override fun getUnlocksForPackageOnDate(packageName: String, date: String): Flow<Int> =
        dao.getUsageForPackageOnDate(packageName, date).map { it?.unlockCount ?: 0 }

    override suspend fun syncFromUsageStats(date: String, resetTimeMinutes: Int) {
        val cal = Calendar.getInstance()
        // Compute start of the effective day
        val resetHour = resetTimeMinutes / 60
        val resetMin = resetTimeMinutes % 60
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, resetHour)
            set(Calendar.MINUTE, resetMin)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis < startCal.timeInMillis) {
            startCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val startMs = startCal.timeInMillis
        val endMs = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startMs, endMs
        ) ?: return

        stats.filter { it.totalTimeInForeground > 0 }.forEach { stat ->
            val existing = dao.getUsageForPackageAndDate(stat.packageName, date)
            val appName = AppInfoUtils.getAppName(context, stat.packageName)
            val entity = AppUsageEntity(
                id = existing?.id ?: 0,
                packageName = stat.packageName,
                appName = appName,
                date = date,
                totalTimeMs = stat.totalTimeInForeground,
                unlockCount = existing?.unlockCount ?: 0,
                screenOnCount = existing?.screenOnCount ?: 0
            )
            dao.upsert(entity)
        }
    }

    override suspend fun incrementUnlock(packageName: String, date: String) {
        dao.incrementUnlockCount(packageName, date)
    }

    override suspend fun incrementScreenOn(date: String) {
        dao.incrementScreenOnCount(date)
    }

    private fun AppUsageEntity.toDomain() = AppUsage(
        packageName = packageName,
        appName = appName,
        date = date,
        totalTimeMs = totalTimeMs,
        unlockCount = unlockCount,
        screenOnCount = screenOnCount
    )
}
