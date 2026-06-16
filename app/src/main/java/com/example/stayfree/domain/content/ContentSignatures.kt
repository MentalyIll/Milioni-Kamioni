package com.example.stayfree.domain.content

/**
 * THE single source of content-detection signatures. Per-content blocking
 * relies on resource-ids inside other apps, which change with every app
 * update — when Reels/Shorts stop triggering, edit ONLY this file.
 *
 * v1 scope: Instagram Reels + YouTube Shorts (per the product plan).
 * - TikTok is entirely short-form → use ordinary app blocking, not this.
 * - Facebook Reels dropped from v1 (most unstable ids, too many misses).
 */
object ContentSignatures {

    const val INSTAGRAM_REELS = "instagram_reels"
    const val YOUTUBE_SHORTS = "youtube_shorts"

    val ALL: List<ContentBlockTarget> = listOf(
        ContentBlockTarget(
            id = INSTAGRAM_REELS,
            displayName = "Reels",
            packageName = "com.instagram.android",
            viewIdSignatures = listOf(
                "clips_viewer",
                "clips_video_container",
                "clips_tab"
            )
        ),
        ContentBlockTarget(
            id = YOUTUBE_SHORTS,
            displayName = "Shorts",
            packageName = "com.google.android.youtube",
            viewIdSignatures = listOf(
                "reel_recycler",
                "reel_player_page_container",
                "reel_watch_pager",
                "shorts"
            )
        )
    )

    /** Packages we ever care about — used to cheaply skip non-target foregrounds. */
    val targetPackages: Set<String> = ALL.map { it.packageName }.toSet()

    fun byId(id: String): ContentBlockTarget? = ALL.firstOrNull { it.id == id }

    fun byPackage(pkg: String): ContentBlockTarget? = ALL.firstOrNull { it.packageName == pkg }
}
