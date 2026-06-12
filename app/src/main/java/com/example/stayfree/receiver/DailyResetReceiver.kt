package com.example.stayfree.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stayfree.worker.DailyResetWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DailyResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<DailyResetWorker>().build())
    }
}
