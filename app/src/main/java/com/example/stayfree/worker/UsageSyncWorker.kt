package com.example.stayfree.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.util.TimeUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UsageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val usageRepository: UsageRepository,
    private val prefs: AppPreferences
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "usage_sync_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            val resetTime = prefs.dailyResetTimeMinutes.first()
            val date = TimeUtils.getEffectiveDate(resetTime)
            usageRepository.syncFromUsageStats(date, resetTime)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
