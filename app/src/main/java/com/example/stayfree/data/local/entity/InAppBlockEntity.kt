package com.example.stayfree.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "inapp_blocks")
data class InAppBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetApp: String,          // e.g. "com.google.android.youtube"
    val featureName: String,        // e.g. "YouTube Shorts"
    val detectionStrategy: String,  // JSON: {"type":"viewId","value":"...","fallback":{...}}
    val isActive: Boolean = false,
    val syncId: String = UUID.randomUUID().toString()
)
