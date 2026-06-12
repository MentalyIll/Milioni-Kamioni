package com.example.stayfree.domain.model

data class AppUsage(
    val packageName: String,
    val appName: String,
    val date: String,
    val totalTimeMs: Long,
    val unlockCount: Int,
    val screenOnCount: Int
)
