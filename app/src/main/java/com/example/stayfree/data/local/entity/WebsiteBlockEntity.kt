package com.example.stayfree.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "website_blocks")
data class WebsiteBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,             // e.g. "youtube.com"
    val dailyCapMs: Long? = null,   // null = always block
    val timeUsedTodayMs: Long = 0,
    val isActive: Boolean = true,
    val date: String = "",          // "YYYY-MM-DD" — resets daily
    val syncId: String = UUID.randomUUID().toString()
)
