package com.example.stayfree.service

import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.receiver.ScreenStateReceiver
import com.example.stayfree.util.NotificationUtils
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class UsageTrackingService : LifecycleService() {

    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var prefs: AppPreferences

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
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Receiver may not be registered if onCreate failed midway.
        }
        syncJob?.cancel()
        serviceScope.cancel()
    }

    private fun registerScreenStateReceiver() {
        screenStateReceiver = ScreenStateReceiver(usageRepository, prefs, serviceScope)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(
            this, screenStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
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
}
