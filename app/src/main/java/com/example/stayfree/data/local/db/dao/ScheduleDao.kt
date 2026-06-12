package com.example.stayfree.data.local.db.dao

import androidx.room.*
import com.example.stayfree.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules WHERE blockRuleId = :ruleId")
    fun getSchedulesForRule(ruleId: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE blockRuleId = :ruleId")
    suspend fun getSchedulesForRuleOnce(ruleId: Long): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE blockRuleId = :ruleId")
    suspend fun deleteForRule(ruleId: Long)
}
