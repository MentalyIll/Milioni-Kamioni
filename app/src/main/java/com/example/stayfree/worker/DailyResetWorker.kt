package com.example.stayfree.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stayfree.data.local.db.dao.AppUsageDao
import com.example.stayfree.data.repository.WebsiteBlockRepository
import com.example.stayfree.util.TimeUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appUsageDao: AppUsageDao,
    private val websiteBlockRepository: WebsiteBlockRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_reset_worker"
        private const val KEEP_DAYS = 90
    }

    override suspend fun doWork(): Result {
        return try {
            val today = TimeUtils.getTodayString()
            // Reset website daily timers
            websiteBlockRepository.resetDailyTimers(today)
            // Purge old usage data (keep 90 days)
            val cutoffDate = TimeUtils.getDateStringDaysAgo(KEEP_DAYS)
            appUsageDao.deleteOlderThan(cutoffDate)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
