package com.example.stayfree.domain.content

/**
 * A short-form content surface that can be blocked *within* a host app
 * (e.g. Reels inside Instagram) without blocking the whole app.
 *
 * @param viewIdSignatures resource-id substrings of the *player/viewer* surface
 *        (NOT the always-present nav tab) — matched case-insensitively against
 *        the live accessibility tree. App-version specific; keep them all here.
 */
data class ContentBlockTarget(
    val id: String,
    val displayName: String,   // shown in the overlay title, e.g. "Reels"
    val packageName: String,
    val viewIdSignatures: List<String>
)
