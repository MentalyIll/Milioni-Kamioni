package com.example.stayfree.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [ForeignKey(
        entity = BlockRuleEntity::class,
        parentColumns = ["id"],
        childColumns = ["blockRuleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("blockRuleId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockRuleId: Long,
    val daysOfWeek: String,         // "MON,TUE,WED" — comma-separated
    val startTimeMinutes: Int,      // minutes from midnight (e.g. 540 = 9:00)
    val endTimeMinutes: Int         // minutes from midnight (e.g. 1020 = 17:00)
)
