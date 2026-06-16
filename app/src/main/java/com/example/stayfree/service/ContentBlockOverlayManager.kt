package com.example.stayfree.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.stayfree.R
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.databinding.OverlayContentBlockBinding
import com.example.stayfree.domain.content.ContentBlockTarget
import com.example.stayfree.util.PinHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Floats a calm "content blocked" card over Reels/Shorts using a
 * TYPE_APPLICATION_OVERLAY window — the host app stays in the background, only
 * the short-form surface is covered. All methods must be called on the main
 * thread (the accessibility service drives this from a Main coroutine).
 */
class ContentBlockOverlayManager(
    private val context: Context,
    private val prefs: AppPreferences,
    private val scope: CoroutineScope,
    private val onGoBack: () -> Unit
) {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var binding: OverlayContentBlockBinding? = null
    private var currentTargetId: String? = null

    /** targetId -> epoch millis until which blocking is bypassed (PIN unlock). */
    private val bypassUntil = mutableMapOf<String, Long>()

    companion object {
        private const val BYPASS_DURATION_MS = 5 * 60_000L
    }

    fun isBypassed(target: ContentBlockTarget): Boolean =
        System.currentTimeMillis() < (bypassUntil[target.id] ?: 0L)

    fun show(target: ContentBlockTarget) {
        if (!Settings.canDrawOverlays(context)) return
        if (currentTargetId == target.id && binding != null) return // already up

        if (binding == null) {
            val view = LayoutInflater.from(context).inflate(R.layout.overlay_content_block, null)
            binding = OverlayContentBlockBinding.bind(view)
            try {
                windowManager.addView(view, params(focusable = false))
            } catch (e: Exception) {
                binding = null
                return
            }
        }
        currentTargetId = target.id
        bindContent(target)
    }

    fun hide() {
        val b = binding ?: return
        try {
            windowManager.removeView(b.root)
        } catch (e: Exception) {
            // Already detached.
        }
        binding = null
        currentTargetId = null
    }

    fun destroy() = hide()

    private fun bindContent(target: ContentBlockTarget) {
        val b = binding ?: return
        b.tvOverlayTitle.text = context.getString(R.string.content_block_title, target.displayName)
        // Reset to the default (non-PIN) state on every (re)bind.
        b.pinGroup.visibility = View.GONE
        b.etOverlayPin.setText("")
        setFocusable(false)

        b.btnOverlayBack.setOnClickListener {
            hide()
            onGoBack()
        }

        // Override button only when a PIN actually exists.
        scope.launch {
            val hasPin = prefs.pinHash.first() != null
            b.btnOverlayBypass.visibility = if (hasPin) View.VISIBLE else View.GONE
        }

        b.btnOverlayBypass.setOnClickListener {
            b.pinGroup.visibility = View.VISIBLE
            b.btnOverlayBypass.visibility = View.GONE
            setFocusable(true)
            b.etOverlayPin.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(b.etOverlayPin, InputMethodManager.SHOW_IMPLICIT)
        }

        b.btnOverlayPinConfirm.setOnClickListener {
            val entered = b.etOverlayPin.text?.toString().orEmpty()
            scope.launch {
                val stored = prefs.pinHash.first()
                if (stored != null && PinHasher.verify(entered, stored)) {
                    bypassUntil[target.id] = System.currentTimeMillis() + BYPASS_DURATION_MS
                    hide()
                } else {
                    b.etOverlayPin.setText("")
                    Toast.makeText(context, R.string.pin_incorrect, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Toggle window focusability so the PIN field can receive the IME. */
    private fun setFocusable(focusable: Boolean) {
        val b = binding ?: return
        try {
            windowManager.updateViewLayout(b.root, params(focusable))
        } catch (e: Exception) {
            // View not attached.
        }
    }

    private fun params(focusable: Boolean): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        var flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        if (!focusable) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }
    }
}
