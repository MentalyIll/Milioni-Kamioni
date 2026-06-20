package com.example.stayfree.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.stayfree.R
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.databinding.ActivityMainBinding
import com.example.stayfree.service.TrackingScheduler
import com.example.stayfree.ui.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var prefs: AppPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Inflate synchronously in onCreate so the NavHostFragment's fragment
        // transaction commits before the activity can ever reach
        // onSaveInstanceState — doing it later from a coroutine risks
        // "Can not perform this action after onSaveInstanceState".
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHostFragment.navController)

        // Keep the splash up (hiding the dashboard) until we've decided whether
        // to route to onboarding instead.
        var routed = false
        splashScreen.setKeepOnScreenCondition { !routed }

        lifecycleScope.launch {
            if (!prefs.onboardingComplete.first()) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
                return@launch
            }
            routed = true
            val resetTime = prefs.dailyResetTimeMinutes.first()
            TrackingScheduler.ensureWorkScheduled(this@MainActivity, resetTime)
            TrackingScheduler.ensureStarted(this@MainActivity)
        }
    }
}
