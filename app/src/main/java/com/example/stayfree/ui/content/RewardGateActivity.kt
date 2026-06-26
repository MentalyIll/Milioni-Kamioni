package com.example.stayfree.ui.content

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stayfree.R
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.databinding.ActivityRewardGateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Reward-unlock gate shown the moment a reward-mode content surface (Instagram
 * Stories for now) is detected. The accessibility service first presses Back so
 * the host app leaves the Story viewer and lands on its normal feed, then starts
 * this activity over it.
 *
 * The user can either leave (Exit → home) or watch a rewarded ad to earn a short
 * window of free access. For the test phase the ad is a stub: tapping "Watch ad"
 * grants the unlock immediately. [showAd] is the integration point for a real
 * AdMob *Rewarded* ad — see the template in the comments below. The real ad needs
 * INTERNET + play-services-ads + an AdMob app id, none of which ship yet (the app
 * is offline by design), so it stays stubbed until release.
 */
@AndroidEntryPoint
class RewardGateActivity : AppCompatActivity() {

    @Inject lateinit var prefs: AppPreferences

    private lateinit var binding: ActivityRewardGateBinding

    companion object {
        const val EXTRA_CONTENT_ID = "extra_content_id"
        const val EXTRA_DISPLAY_NAME = "extra_display_name"

        /** How long a single rewarded unlock lasts. */
        const val GRACE_MS = 3 * 60 * 1000L
        /** Max rewarded unlocks per day — beyond this the ad button is hidden. */
        const val DAILY_CAP = 5

        fun newIntent(context: Context, contentId: String, displayName: String): Intent =
            Intent(context, RewardGateActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(EXTRA_CONTENT_ID, contentId)
                putExtra(EXTRA_DISPLAY_NAME, displayName)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardGateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        goImmersive()

        val displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME) ?: getString(R.string.app_name)
        binding.tvTitle.text = getString(R.string.reward_gate_title, displayName)

        binding.btnWatchAd.setOnClickListener { onWatchAd() }
        binding.btnExit.setOnClickListener { goHome() }

        // Back must not return into the Story they just left — send them home.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })

        // Hide the ad button once the daily cap is hit.
        lifecycleScope.launch {
            val used = prefs.unlocksUsedToday(todayKey())
            if (used >= DAILY_CAP) showCapReached()
        }
    }

    private fun onWatchAd() {
        binding.btnWatchAd.isEnabled = false
        showAd()
    }

    /**
     * Rewarded ad template (STUB for now — grants the reward immediately).
     *
     * When the ad SDK is added, preload a RewardedAd in the service and show it
     * here:
     *
     *   rewardedAd?.show(this) { _ -> grantUnlockAndFinish() }   // onUserEarnedReward
     *   // also set fullScreenContentCallback to grantUnlockAndFinish()'s inverse
     *   // (dismiss WITHOUT reward → just re-enable the button, overlay stays)
     *
     * Test ad unit (Google rewarded): ca-app-pub-3940256099942544/5224354917
     * Requires: INTERNET permission, com.google.android.gms:play-services-ads,
     * and the AdMob app id meta-data in the manifest.
     */
    private fun showAd() {
        // Stub: pretend the user finished the rewarded ad right away.
        grantUnlockAndFinish()
    }

    /** Reward earned → persist the timed unlock and drop back onto the host feed. */
    private fun grantUnlockAndFinish() {
        lifecycleScope.launch {
            val granted = prefs.grantContentUnlock(GRACE_MS, DAILY_CAP, todayKey())
            if (granted) {
                // Just finish — the host app's feed is directly behind us, so the
                // user lands back where they were, now free to browse for 3 min.
                finish()
                overridePendingTransition(0, 0)
            } else {
                showCapReached()
            }
        }
    }

    private fun showCapReached() {
        binding.btnWatchAd.visibility = View.GONE
        binding.tvMessage.text = getString(R.string.reward_gate_cap)
    }

    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun todayKey(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun goImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(
                android.view.WindowInsets.Type.statusBars() or
                        android.view.WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}
