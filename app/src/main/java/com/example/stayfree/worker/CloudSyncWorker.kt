package com.example.stayfree.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.BlockingRepository
import com.example.stayfree.data.repository.InAppBlockRepository
import com.example.stayfree.data.repository.WebsiteBlockRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val blockingRepository: BlockingRepository,
    private val websiteBlockRepository: WebsiteBlockRepository,
    private val inAppBlockRepository: InAppBlockRepository,
    private val prefs: AppPreferences
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "cloud_sync_worker"
    }

    override suspend fun doWork(): Result {
        val syncEnabled = prefs.syncEnabled.first()
        if (!syncEnabled) return Result.success()

        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val uid = user.uid
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(uid)

        return try {
            // Push block rules
            val rules = blockingRepository.getActiveRulesOnce()
            val rulesCollection = userRef.collection("block_rules")
            rules.forEach { rule ->
                val data = mapOf(
                    "packageName" to rule.packageName,
                    "blockType" to rule.blockType,
                    "isActive" to rule.isActive,
                    "isPinLocked" to rule.isPinLocked,
                    "dailyLimitMs" to rule.dailyLimitMs,
                    "sessionLimitMs" to rule.sessionLimitMs,
                    "sessionBreakMs" to rule.sessionBreakMs,
                    "createdAt" to rule.createdAt
                )
                rulesCollection.document(rule.syncId).set(data, SetOptions.merge()).await()
            }

            // Push website blocks
            val websites = websiteBlockRepository.getActiveOnce()
            val websitesCollection = userRef.collection("website_blocks")
            websites.forEach { site ->
                val data = mapOf(
                    "domain" to site.domain,
                    "dailyCapMs" to site.dailyCapMs,
                    "isActive" to site.isActive
                )
                websitesCollection.document(site.syncId).set(data, SetOptions.merge()).await()
            }

            // Push in-app blocks
            val inAppBlocks = inAppBlockRepository.getActiveOnce()
            val inAppCollection = userRef.collection("inapp_blocks")
            inAppBlocks.forEach { block ->
                val data = mapOf(
                    "targetApp" to block.targetApp,
                    "featureName" to block.featureName,
                    "detectionStrategy" to block.detectionStrategy,
                    "isActive" to block.isActive
                )
                inAppCollection.document(block.syncId).set(data, SetOptions.merge()).await()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
