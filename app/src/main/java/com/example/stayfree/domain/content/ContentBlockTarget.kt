package com.example.stayfree.domain.content

/**
 * How a detected content surface is handled.
 */
enum class ContentBlockMode {
    /** Cover the surface with a full-screen block the user can't easily bypass. */
    HARD_BLOCK,
    /** Offer a rewarded unlock: watch an ad → a short window of free access. */
    REWARD_UNLOCK
}

/**
 * A short-form content surface that can be blocked *within* a host app
 * (e.g. Reels inside Instagram) without blocking the whole app.
 *
 * @param viewIdSignatures resource-id substrings of the *player/viewer* surface
 *        (NOT the always-present nav tab) — matched case-insensitively against
 *        the live accessibility tree. App-version specific; keep them all here.
 * @param blockMode whether the surface is hard-blocked or offers a rewarded unlock.
 */
data class ContentBlockTarget(
    val id: String,
    val displayName: String,   // shown in the overlay title, e.g. "Reels"
    val packageName: String,
    val viewIdSignatures: List<String>,
    val blockMode: ContentBlockMode = ContentBlockMode.HARD_BLOCK
)
