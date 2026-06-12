package com.example.stayfree.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.data.repository.BlockingRepository
import com.example.stayfree.data.repository.InAppBlockRepository
import com.example.stayfree.data.repository.UsageRepository
import com.example.stayfree.data.repository.WebsiteBlockRepository
import com.example.stayfree.domain.model.BlockType
import com.example.stayfree.ui.blocking.FocusModeState
import com.example.stayfree.ui.overlay.BlockOverlayActivity
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class StayFreeAccessibilityService : AccessibilityService() {

    @Inject lateinit var blockingRepository: BlockingRepository
    @Inject lateinit var websiteBlockRepository: WebsiteBlockRepository
    @Inject lateinit var inAppBlockRepository: InAppBlockRepository
    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var prefs: AppPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // In-memory cache of blocked packages — refreshed every 30s
    private val blockedPackagesCache = mutableSetOf<String>()
    // In-memory usage accumulation — more accurate than UsageStatsManager alone
    private var currentForegroundPackage: String? = null
    private var foregroundSince: Long = 0L
    private val usageAccumulator = mutableMapOf<String, Long>()
    // Debounce per package (last blocked timestamp)
    private val lastBlockedTime = mutableMapOf<String, Long>()
    // Per-package last check for in-app blocks (debounce)
    private val lastInAppCheckTime = mutableMapOf<String, Long>()
    // Website time tracking
    private var currentBrowserDomain: String? = null
    private var domainStartTime: Long = 0L
    private var urlDebounceJob: Job? = null
    // Focus mode state (loaded from DataStore)
    private var focusModeActive = false
    private var focusModeEndTime = 0L
    private var focusModeWhitelist = emptySet<String>()
    private var focusModeIsWhitelist = true // true=whitelist, false=blacklist

    companion object {
        private const val BLOCK_DEBOUNCE_MS = 500L
        private const val CACHE_REFRESH_INTERVAL_MS = 30_000L
        private const val USAGE_PERSIST_INTERVAL_MS = 60_000L
        private const val INAPP_CHECK_DEBOUNCE_MS = 500L
        private const val URL_DEBOUNCE_MS = 500L

        // Browser URL bar view IDs
        private val BROWSER_URL_VIEW_IDS = mapOf(
            "com.android.chrome" to "com.android.chrome:id/url_bar",
            "org.mozilla.firefox" to "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "com.microsoft.emmx" to "com.microsoft.emmx:id/url_bar",
            "com.sec.android.app.sbrowser" to "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.brave.browser" to "com.brave.browser:id/url_bar",
            "com.opera.browser" to "com.opera.browser:id/url_field"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startCacheRefresh()
        startUsagePersist()
        collectFocusModeState()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // ignore self

        val eventType = event.eventType
        val now = System.currentTimeMillis()

        // Track foreground time accumulation
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            trackForegroundTime(pkg, now)
        }

        // Debounce: check no more than once per 500ms per package
        val lastBlock = lastBlockedTime[pkg] ?: 0L
        if (now - lastBlock < BLOCK_DEBOUNCE_MS) return

        serviceScope.launch(Dispatchers.Main) {
            // Check focus mode
            if (focusModeActive && now < focusModeEndTime) {
                val shouldBlock = if (focusModeIsWhitelist) {
                    pkg !in focusModeWhitelist
                } else {
                    pkg in focusModeWhitelist
                }
                if (shouldBlock) {
                    showBlockOverlay(pkg, "FOCUS")
                    lastBlockedTime[pkg] = now
                    return@launch
                }
            }

            // Check standard block rules
            if (pkg in blockedPackagesCache) {
                if (isPackageCurrentlyBlocked(pkg)) {
                    showBlockOverlay(pkg, getBlockReason(pkg))
                    lastBlockedTime[pkg] = now
                    return@launch
                }
            }

            // Website blocking (only for known browsers)
            if (pkg in BROWSER_URL_VIEW_IDS && eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                handleBrowserEvent(event, pkg)
            }

            // In-app blocking
            val lastInAppCheck = lastInAppCheckTime[pkg] ?: 0L
            if (now - lastInAppCheck > INAPP_CHECK_DEBOUNCE_MS) {
                lastInAppCheckTime[pkg] = now
                handleInAppBlock(event, pkg)
            }
        }
    }

    private fun trackForegroundTime(newPkg: String, now: Long) {
        currentForegroundPackage?.let { prevPkg ->
            val elapsed = now - foregroundSince
            if (elapsed > 0) {
                usageAccumulator[prevPkg] = (usageAccumulator[prevPkg] ?: 0L) + elapsed
            }
        }
        currentForegroundPackage = newPkg
        foregroundSince = now
    }

    private suspend fun isPackageCurrentlyBlocked(pkg: String): Boolean {
        val resetTime = prefs.dailyResetTimeMinutes.first()
        val date = TimeUtils.getEffectiveDate(resetTime)
        val rules = blockingRepository.getActiveRulesForPackage(pkg)

        for (rule in rules) {
            when (rule.blockType) {
                "BLOCK_NOW" -> return true
                "SCHEDULED" -> {
                    val schedules = blockingRepository.getSchedulesForRule(rule.id)
                    if (schedules.any {
                            TimeUtils.isInScheduleWindow(it.daysOfWeek, it.startTimeMinutes, it.endTimeMinutes)
                        }) return true
                }
                "DAILY_LIMIT" -> {
                    val limit = rule.dailyLimitMs ?: continue
                    // Use accumulator + DB value
                    val accMs = usageAccumulator[pkg] ?: 0L
                    // We'll do a quick DB check here
                    return accMs >= limit
                }
                "SESSION" -> {
                    val limit = rule.sessionLimitMs ?: continue
                    val sessionStart = foregroundSince
                    if (currentForegroundPackage == pkg && (System.currentTimeMillis() - sessionStart) >= limit) {
                        return true
                    }
                }
                "SLEEP" -> {
                    val schedules = blockingRepository.getSchedulesForRule(rule.id)
                    if (schedules.any {
                            TimeUtils.isInScheduleWindow(it.daysOfWeek, it.startTimeMinutes, it.endTimeMinutes)
                        }) return true
                }
                else -> {}
            }
        }
        return false
    }

    private suspend fun getBlockReason(pkg: String): String {
        val rules = blockingRepository.getActiveRulesForPackage(pkg)
        return rules.firstOrNull()?.blockType ?: "BLOCK_NOW"
    }

    private fun showBlockOverlay(pkg: String, reason: String) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, pkg)
            putExtra(BlockOverlayActivity.EXTRA_BLOCK_REASON, reason)
        }
        startActivity(intent)
    }

    private fun handleBrowserEvent(event: AccessibilityEvent, pkg: String) {
        urlDebounceJob?.cancel()
        urlDebounceJob = serviceScope.launch {
            delay(URL_DEBOUNCE_MS)
            val rootNode = rootInActiveWindow ?: return@launch
            try {
                val urlViewId = BROWSER_URL_VIEW_IDS[pkg] ?: return@launch
                val urlNodes = rootNode.findAccessibilityNodeInfosByViewId(urlViewId)
                val urlText = urlNodes.firstOrNull()?.text?.toString() ?: return@launch
                urlNodes.forEach { it.recycle() }

                val domain = extractDomain(urlText) ?: return@launch
                checkWebsiteDomain(domain, pkg)
            } finally {
                rootNode.recycle()
            }
        }
    }

    private suspend fun checkWebsiteDomain(domain: String, browserPkg: String) {
        val resetTime = prefs.dailyResetTimeMinutes.first()
        val date = TimeUtils.getEffectiveDate(resetTime)
        val now = System.currentTimeMillis()

        // Update time for previous domain
        if (currentBrowserDomain != null && currentBrowserDomain != domain) {
            val elapsed = now - domainStartTime
            val prevBlock = websiteBlockRepository.getByDomain(currentBrowserDomain!!)
            if (prevBlock != null) {
                websiteBlockRepository.updateTimeUsed(prevBlock.id, prevBlock.timeUsedTodayMs + elapsed, date)
            }
        }

        if (currentBrowserDomain != domain) {
            currentBrowserDomain = domain
            domainStartTime = now
        }

        val websiteBlock = websiteBlockRepository.getByDomain(domain) ?: return
        if (!websiteBlock.isActive) return

        if (websiteBlock.dailyCapMs == null) {
            // Always block
            showBlockOverlay(browserPkg, "WEBSITE_BLOCKED")
        } else {
            val elapsed = now - domainStartTime
            val totalUsed = websiteBlock.timeUsedTodayMs + elapsed
            if (totalUsed >= websiteBlock.dailyCapMs) {
                showBlockOverlay(browserPkg, "WEBSITE_CAP_REACHED")
            }
        }
    }

    private fun handleInAppBlock(event: AccessibilityEvent, pkg: String) {
        serviceScope.launch {
            val targets = inAppBlockRepository.getActiveForPackage(pkg)
            if (targets.isEmpty()) return@launch

            val rootNode = rootInActiveWindow ?: return@launch
            try {
                for (target in targets) {
                    if (checkInAppStrategy(rootNode, target.detectionStrategy)) {
                        withContext(Dispatchers.Main) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                        break
                    }
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    private fun checkInAppStrategy(rootNode: AccessibilityNodeInfo, strategyJson: String): Boolean {
        return try {
            val strategy = JSONObject(strategyJson)
            val matched = matchNodeStrategy(rootNode, strategy)
            if (!matched) {
                val fallback = strategy.optJSONObject("fallback")
                if (fallback != null) matchNodeStrategy(rootNode, fallback) else false
            } else matched
        } catch (e: Exception) {
            false
        }
    }

    private fun matchNodeStrategy(rootNode: AccessibilityNodeInfo, strategy: JSONObject): Boolean {
        val type = strategy.optString("type")
        val value = strategy.optString("value")
        return when (type) {
            "viewId" -> {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(value)
                val found = nodes.isNotEmpty()
                nodes.forEach { it.recycle() }
                found
            }
            "contentDescription" -> {
                val nodes = rootNode.findAccessibilityNodeInfosByText(value)
                val found = nodes.any { it.contentDescription?.toString()?.contains(value, ignoreCase = true) == true }
                nodes.forEach { it.recycle() }
                found
            }
            "text" -> {
                val nodes = rootNode.findAccessibilityNodeInfosByText(value)
                val found = nodes.isNotEmpty()
                nodes.forEach { it.recycle() }
                found
            }
            else -> false
        }
    }

    private fun extractDomain(url: String): String? {
        return try {
            val cleaned = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
            val domain = cleaned.substringBefore("/").substringBefore("?").substringBefore("#")
            if (domain.contains(".") && domain.length > 3) domain else null
        } catch (e: Exception) {
            null
        }
    }

    private fun startCacheRefresh() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val rules = blockingRepository.getActiveRulesOnce()
                    val packages = rules.map { it.packageName }.toSet()
                    blockedPackagesCache.clear()
                    blockedPackagesCache.addAll(packages)
                } catch (e: Exception) { /* continue */ }
                delay(CACHE_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun startUsagePersist() {
        serviceScope.launch {
            while (isActive) {
                delay(USAGE_PERSIST_INTERVAL_MS)
                try {
                    val resetTime = prefs.dailyResetTimeMinutes.first()
                    val date = TimeUtils.getEffectiveDate(resetTime)
                    // Flush current foreground package
                    currentForegroundPackage?.let { pkg ->
                        val now = System.currentTimeMillis()
                        val elapsed = now - foregroundSince
                        usageAccumulator[pkg] = (usageAccumulator[pkg] ?: 0L) + elapsed
                        foregroundSince = now
                    }
                    // Persist all accumulated time to DB
                    for ((pkg, ms) in usageAccumulator) {
                        if (ms > 0) {
                            usageRepository.updateAccumulatedTime(pkg, date, ms)
                        }
                    }
                    usageAccumulator.clear()
                } catch (e: Exception) { /* continue */ }
            }
        }
    }

    private fun collectFocusModeState() {
        serviceScope.launch {
            FocusModeState.state.collect { snapshot ->
                focusModeActive = snapshot.active
                focusModeEndTime = snapshot.endTime
                focusModeWhitelist = snapshot.whitelist
                focusModeIsWhitelist = snapshot.isWhitelist
            }
        }
    }

    fun updateFocusMode(active: Boolean, endTime: Long, whitelist: Set<String>, isWhitelist: Boolean) {
        focusModeActive = active
        focusModeEndTime = endTime
        focusModeWhitelist = whitelist
        focusModeIsWhitelist = isWhitelist
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
