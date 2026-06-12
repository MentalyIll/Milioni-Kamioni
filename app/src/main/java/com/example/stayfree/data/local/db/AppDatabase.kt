package com.example.stayfree.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stayfree.data.local.db.dao.*
import com.example.stayfree.data.local.entity.*

@Database(
    entities = [
        AppUsageEntity::class,
        BlockRuleEntity::class,
        ScheduleEntity::class,
        WebsiteBlockEntity::class,
        InAppBlockEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun blockRuleDao(): BlockRuleDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun websiteBlockDao(): WebsiteBlockDao
    abstract fun inAppBlockDao(): InAppBlockDao
    abstract fun sessionDao(): SessionDao
}
