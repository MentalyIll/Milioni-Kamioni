package com.example.stayfree.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayfree.data.local.db.dao.AppUsageDao
import com.example.stayfree.data.local.db.dao.BlockRuleDao
import com.example.stayfree.data.local.db.dao.InAppBlockDao
import com.example.stayfree.data.local.db.dao.WebsiteBlockDao
import com.example.stayfree.data.local.preferences.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val pinEnabled: StateFlow<Boolean> = prefs.pinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val syncEnabled: StateFlow<Boolean> = prefs.syncEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dailyResetTime: StateFlow<Int> = prefs.dailyResetTimeMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userId: StateFlow<String?> = prefs.userId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDailyResetTime(minutes: Int) {
        viewModelScope.launch { prefs.setDailyResetTime(minutes) }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setSyncEnabled(enabled) }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        viewModelScope.launch { prefs.setUserId(null) }
    }
}
