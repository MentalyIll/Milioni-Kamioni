package com.example.stayfree.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stayfree.data.repository.BlockingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {

    @Inject lateinit var blockingRepository: BlockingRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pkg = intent.data?.schemeSpecificPart ?: return
        if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
            CoroutineScope(Dispatchers.IO).launch {
                blockingRepository.deactivateAllForPackage(pkg)
            }
        }
    }
}
