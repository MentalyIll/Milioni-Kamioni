package com.example.stayfree.ui.inapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.entity.InAppBlockEntity
import com.example.stayfree.data.repository.InAppBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppBlockViewModel @Inject constructor(
    private val repository: InAppBlockRepository
) : ViewModel() {

    val allTargets: StateFlow<List<InAppBlockEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initDefaultTargets() {
        viewModelScope.launch {
            // Detection targets the on-screen *player container* (matched by a
            // substring of its resource-id) rather than the always-present bottom
            // nav tab — otherwise the whole app would be blocked from its home
            // screen. resource-ids are app-version specific, so each target lists
            // several historical candidates via "anyOf".
            val defaults = listOf(
                "com.google.android.youtube" to ("YouTube Shorts" to
                    """{"type":"anyOf","strategies":[{"type":"viewIdContains","value":"reel_recycler"},{"type":"viewIdContains","value":"reel_watch_pager"},{"type":"viewIdContains","value":"reel_player_page_container"},{"type":"viewIdContains","value":"shorts_video"}]}"""),
                "com.instagram.android" to ("Instagram Reels" to
                    """{"type":"anyOf","strategies":[{"type":"viewIdContains","value":"clips_viewer"},{"type":"viewIdContains","value":"reel_viewer"},{"type":"viewIdContains","value":"clips_tab_recycler"}]}"""),
                "com.snapchat.android" to ("Snapchat Spotlight" to
                    """{"type":"anyOf","strategies":[{"type":"viewIdContains","value":"spotlight"},{"type":"viewIdContains","value":"discover_feed"}]}"""),
                "com.zhiliaoapp.musically" to ("TikTok For You" to
                    """{"type":"anyOf","strategies":[{"type":"viewIdContains","value":"feed_recycler"},{"type":"viewIdContains","value":"video_feed"}]}"""),
                "com.facebook.katana" to ("Facebook Reels" to
                    """{"type":"viewIdContains","value":"reels"}"""),
                "com.twitter.android" to ("Twitter Trending" to
                    """{"type":"anyOf","strategies":[{"type":"viewIdContains","value":"explore"},{"type":"contentDescription","value":"Trending"}]}""")
            )

            val existing = repository.getAllOnce()
            for ((targetApp, feature) in defaults) {
                val (featureName, strategy) = feature
                val matches = existing.filter { it.targetApp == targetApp && it.featureName == featureName }
                if (matches.isEmpty()) {
                    repository.insert(
                        InAppBlockEntity(targetApp = targetApp, featureName = featureName, detectionStrategy = strategy)
                    )
                } else {
                    // Keep one row (prefer an already-enabled one), refresh its
                    // detection strategy, and drop any duplicates from older builds.
                    val keep = matches.firstOrNull { it.isActive } ?: matches.first()
                    repository.updateStrategy(keep.id, strategy)
                    matches.filter { it.id != keep.id }.forEach { repository.deleteById(it.id) }
                }
            }
        }
    }

    fun toggleTarget(entity: InAppBlockEntity) {
        viewModelScope.launch {
            repository.setActive(entity.id, !entity.isActive)
        }
    }
}
