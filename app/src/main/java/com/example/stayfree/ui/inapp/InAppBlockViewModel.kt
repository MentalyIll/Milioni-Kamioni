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
            val defaults = listOf(
                InAppBlockEntity(
                    targetApp = "com.google.android.youtube",
                    featureName = "YouTube Shorts",
                    detectionStrategy = """{"type":"viewId","value":"com.google.android.youtube:id/reel_pivot_bar_container","fallback":{"type":"contentDescription","value":"Shorts"}}"""
                ),
                InAppBlockEntity(
                    targetApp = "com.instagram.android",
                    featureName = "Instagram Reels",
                    detectionStrategy = """{"type":"contentDescription","value":"Reels","fallback":{"type":"viewId","value":"com.instagram.android:id/clips_tab"}}"""
                ),
                InAppBlockEntity(
                    targetApp = "com.snapchat.android",
                    featureName = "Snapchat Spotlight",
                    detectionStrategy = """{"type":"contentDescription","value":"Spotlight"}"""
                ),
                InAppBlockEntity(
                    targetApp = "com.zhiliaoapp.musically",
                    featureName = "TikTok For You",
                    detectionStrategy = """{"type":"contentDescription","value":"For You"}"""
                ),
                InAppBlockEntity(
                    targetApp = "com.facebook.katana",
                    featureName = "Facebook Reels",
                    detectionStrategy = """{"type":"contentDescription","value":"Reels"}"""
                ),
                InAppBlockEntity(
                    targetApp = "com.twitter.android",
                    featureName = "Twitter Trending",
                    detectionStrategy = """{"type":"contentDescription","value":"Trending","fallback":{"type":"viewId","value":"com.twitter.android:id/explore_tab"}}"""
                )
            )
            defaults.forEach { target ->
                val existing = repository.getAll()
                // Only insert if not already present (by targetApp + featureName)
                repository.insert(target)
            }
        }
    }

    fun toggleTarget(entity: InAppBlockEntity) {
        viewModelScope.launch {
            repository.setActive(entity.id, !entity.isActive)
        }
    }
}
