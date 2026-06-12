package com.example.stayfree.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stayfree.util.PermissionUtils
import com.example.stayfree.worker.DailyResetWorker
import com.example.stayfree.worker.UsageSyncWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Single entry point for starting the tracking foreground service and the
 * periodic WorkManager jobs. Safe to call repeatedly from any process state:
 * work is enqueued with KEEP and the service start is permission-guarded.
 */
object TrackingScheduler {

    fun ensureStarted(context: Context) {
        if (!PermissionUtils.hasUsageStatsPermission(context)) return
        val intent = Intent(context, UsageTrackingService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Background-start restrictions (e.g. ForegroundServiceStartNotAllowedException).
            // Periodic workers keep usage data flowing until the next foreground launch.
        }
    }

    fun ensureWorkScheduled(context: Context, resetTimeMinutes: Int = 0) {
        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniquePeriodicWork(
            UsageSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UsageSyncWorker>(15, TimeUnit.MINUTES).build()
        )

        workManager.enqueueUniquePeriodicWork(
            DailyResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DailyResetWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntilNextReset(resetTimeMinutes), TimeUnit.MILLISECONDS)
                .build()
        )
    }

    private fun millisUntilNextReset(resetTimeMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, resetTimeMinutes / 60)
            set(Calendar.MINUTE, resetTimeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }
}
