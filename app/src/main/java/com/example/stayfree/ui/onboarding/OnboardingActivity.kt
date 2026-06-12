package com.example.stayfree.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.databinding.ActivityOnboardingBinding
import com.example.stayfree.ui.MainActivity
import com.example.stayfree.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    @Inject lateinit var prefs: AppPreferences

    private lateinit var binding: ActivityOnboardingBinding

    private val steps = listOf(
        OnboardingStep.USAGE_ACCESS,
        OnboardingStep.ACCESSIBILITY,
        OnboardingStep.OVERLAY,
        OnboardingStep.NOTIFICATIONS,
        OnboardingStep.BATTERY
    )
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showStep(currentStep)

        binding.btnGrant.setOnClickListener { requestCurrentPermission() }
        binding.btnNext.setOnClickListener { advance() }
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
    }

    private fun showStep(index: Int) {
        val step = steps[index]
        binding.tvTitle.setText(step.titleRes)
        binding.tvDescription.setText(step.descriptionRes)
        binding.tvStepIndicator.text = "${index + 1} / ${steps.size}"
        updateButtonState()
    }

    private fun updateButtonState() {
        val granted = isCurrentStepGranted()
        binding.btnGrant.isEnabled = !granted
        binding.btnNext.isEnabled = granted
    }

    private fun isCurrentStepGranted(): Boolean = when (steps[currentStep]) {
        OnboardingStep.USAGE_ACCESS -> PermissionUtils.hasUsageStatsPermission(this)
        OnboardingStep.ACCESSIBILITY -> PermissionUtils.hasAccessibilityServiceEnabled(this)
        OnboardingStep.OVERLAY -> PermissionUtils.hasOverlayPermission(this)
        OnboardingStep.NOTIFICATIONS -> PermissionUtils.hasNotificationPermission(this)
        OnboardingStep.BATTERY -> PermissionUtils.isIgnoringBatteryOptimizations(this)
    }

    private fun requestCurrentPermission() {
        when (steps[currentStep]) {
            OnboardingStep.USAGE_ACCESS ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            OnboardingStep.ACCESSIBILITY ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            OnboardingStep.OVERLAY ->
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            OnboardingStep.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
                }
            }
            OnboardingStep.BATTERY ->
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")))
        }
    }

    private fun advance() {
        currentStep++
        if (currentStep >= steps.size) {
            lifecycleScope.launch {
                prefs.setOnboardingComplete(true)
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        } else {
            showStep(currentStep)
        }
    }
}
