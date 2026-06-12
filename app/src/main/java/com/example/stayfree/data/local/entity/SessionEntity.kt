package com.example.stayfree.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMs: Long? = null,
    val date: String               // "YYYY-MM-DD"
)
