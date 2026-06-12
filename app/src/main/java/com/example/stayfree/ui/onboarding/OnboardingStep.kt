package com.example.stayfree.ui.onboarding

import com.example.stayfree.R

enum class OnboardingStep(val titleRes: Int, val descriptionRes: Int) {
    USAGE_ACCESS(R.string.onboarding_usage_title, R.string.onboarding_usage_desc),
    ACCESSIBILITY(R.string.onboarding_accessibility_title, R.string.onboarding_accessibility_desc),
    OVERLAY(R.string.onboarding_overlay_title, R.string.onboarding_overlay_desc),
    NOTIFICATIONS(R.string.onboarding_notifications_title, R.string.onboarding_notifications_desc),
    BATTERY(R.string.onboarding_battery_title, R.string.onboarding_battery_desc)
}
