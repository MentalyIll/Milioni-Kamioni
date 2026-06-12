package com.example.stayfree.domain.model

data class WebsiteBlock(
    val id: Long,
    val domain: String,
    val dailyCapMs: Long?,       // null = always block
    val timeUsedTodayMs: Long,
    val isActive: Boolean,
    val syncId: String
)
