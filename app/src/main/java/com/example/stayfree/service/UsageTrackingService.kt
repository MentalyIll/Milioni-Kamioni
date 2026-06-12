package com.example.stayfree.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.LifecycleService
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.receiver.DailyResetReceiver
import com.example.stayfree.receiver.ScreenStateReceiver
import com.example.stayfree.util.NotificationUtils
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class UsageTrackingService : LifecycleService() {

    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var prefs: AppPreferences
    @Inject lateinit var alarmManager: AlarmManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var screenStateReceiver: ScreenStateReceiver
    private var syncJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NotificationUtils.NOTIFICATION_ID_FOREGROUND,
            NotificationUtils.buildForegroundNotification(this)
        )
        registerScreenStateReceiver()
        startPeriodicSync()
        scheduleDailyReset()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
        syncJob?.cancel()
        serviceScope.cancel()
    }

    private fun registerScreenStateReceiver() {
        screenStateReceiver = ScreenStateReceiver(usageRepository, prefs, serviceScope)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    private fun startPeriodicSync() {
        syncJob = serviceScope.launch {
            while (isActive) {
                try {
                    val resetTime = prefs.dailyResetTimeMinutes.first()
                    val date = TimeUtils.getEffectiveDate(resetTime)
                    usageRepository.syncFromUsageStats(date, resetTime)
                } catch (e: Exception) {
                    // continue on error
                }
                delay(60_000L)
            }
        }
    }

    private fun scheduleDailyReset() {
        serviceScope.launch {
            val resetTimeMinutes = prefs.dailyResetTimeMinutes.first()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, resetTimeMinutes / 60)
                set(Calendar.MINUTE, resetTimeMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val intent = Intent(this@UsageTrackingService, DailyResetReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this@UsageTrackingService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        }
    }
}
