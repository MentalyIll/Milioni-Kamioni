package com.example.stayfree.data.local.db.dao

import androidx.room.*
import com.example.stayfree.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE packageName = :pkg AND date = :date ORDER BY startTime DESC")
    fun getSessionsForPackageAndDate(pkg: String, date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE packageName = :pkg AND endTime IS NULL LIMIT 1")
    suspend fun getOpenSession(pkg: String): SessionEntity?

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("UPDATE sessions SET endTime = :endTime, durationMs = :durationMs WHERE id = :id")
    suspend fun closeSession(id: Long, endTime: Long, durationMs: Long)

    @Query("SELECT SUM(durationMs) FROM sessions WHERE packageName = :pkg AND date = :date")
    suspend fun getTotalSessionDurationForDate(pkg: String, date: String): Long?

    @Query("DELETE FROM sessions WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)
}
