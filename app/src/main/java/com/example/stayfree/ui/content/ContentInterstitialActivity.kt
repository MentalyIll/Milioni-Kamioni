package com.example.stayfree.ui.content

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.stayfree.databinding.ActivityContentInterstitialBinding

/**
 * Full-screen interstitial shown the moment a blocked short-form surface
 * (YouTube Shorts / Instagram Reels) is detected. The accessibility service
 * first sends the host app to the background, then starts this activity, so the
 * user lands here instead of on the feed.
 *
 * For now the surface is a plain black screen ("crna slika"). The [showAd] hook
 * is the integration point for an AdMob Interstitial — see the template below.
 * The real ad needs INTERNET + play-services-ads + an AdMob app id, none of
 * which ship yet (the app is offline by design), so it is intentionally stubbed.
 */
class ContentInterstitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentInterstitialBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoDismiss = Runnable { dismiss() }

    companion object {
        private const val TAG = "MoreMoneyA11y"
        const val EXTRA_CONTENT_ID = "extra_content_id"
        const val EXTRA_DISPLAY_NAME = "extra_display_name"

        /** Safety valve: never trap the user — auto-close after this long. */
        private const val AUTO_DISMISS_MS = 8_000L

        fun newIntent(context: Context, contentId: String, displayName: String): Intent =
            Intent(context, ContentInterstitialActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(EXTRA_CONTENT_ID, contentId)
                putExtra(EXTRA_DISPLAY_NAME, displayName)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Interstitial onCreate (${intent.getStringExtra(EXTRA_DISPLAY_NAME)})")
        binding = ActivityContentInterstitialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        goImmersive()

        binding.btnClose.setOnClickListener { dismiss() }

        // Back returns the user home (never back into the Short they just left).
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = dismiss()
        })

        showAd()
        mainHandler.postDelayed(autoDismiss, AUTO_DISMISS_MS)
    }

    /**
     * AdMob Interstitial template.
     *
     * When the ad SDK is added, replace the placeholder with:
     *
     *   InterstitialAd.load(this, AD_UNIT_ID, AdRequest.Builder().build(),
     *       object : InterstitialAdLoadCallback() {
     *           override fun onAdLoaded(ad: InterstitialAd) {
     *               ad.fullScreenContentCallback = object : FullScreenContentCallback() {
     *                   override fun onAdDismissedFullScreenContent() = dismiss()
     *               }
     *               ad.show(this@ContentInterstitialActivity)
     *           }
     *           override fun onAdFailedToLoad(e: LoadAdError) = Unit // keep black + close button
     *       })
     *
     * Test ad unit (Google): ca-app-pub-3940256099942544/1033173712
     * Requires: INTERNET permission, com.google.android.gms:play-services-ads,
     * and the AdMob app id meta-data in the manifest.
     */
    private fun showAd() {
        // No-op placeholder for now — the layout already shows the black "AD" slot.
    }

    /** Close the interstitial and make sure we end up on the launcher. */
    private fun dismiss() {
        mainHandler.removeCallbacks(autoDismiss)
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(autoDismiss)
        super.onDestroy()
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
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}
