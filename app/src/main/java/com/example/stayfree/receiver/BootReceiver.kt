package com.example.stayfree.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.service.TrackingScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: AppPreferences

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resetTime = prefs.dailyResetTimeMinutes.first()
                TrackingScheduler.ensureWorkScheduled(context, resetTime)
                TrackingScheduler.ensureStarted(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
