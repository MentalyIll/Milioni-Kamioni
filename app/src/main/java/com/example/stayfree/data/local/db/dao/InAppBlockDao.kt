package com.example.stayfree.data.local.db.dao

import androidx.room.*
import com.example.stayfree.data.local.entity.InAppBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InAppBlockDao {

    @Query("SELECT * FROM inapp_blocks ORDER BY featureName ASC")
    fun getAll(): Flow<List<InAppBlockEntity>>

    @Query("SELECT * FROM inapp_blocks ORDER BY id ASC")
    suspend fun getAllOnce(): List<InAppBlockEntity>

    @Query("SELECT * FROM inapp_blocks WHERE isActive = 1")
    suspend fun getActiveOnce(): List<InAppBlockEntity>

    @Query("DELETE FROM inapp_blocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM inapp_blocks WHERE targetApp = :pkg AND isActive = 1")
    suspend fun getActiveForPackage(pkg: String): List<InAppBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InAppBlockEntity): Long

    @Update
    suspend fun update(entity: InAppBlockEntity)

    @Query("UPDATE inapp_blocks SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE inapp_blocks SET detectionStrategy = :strategy WHERE id = :id")
    suspend fun updateStrategy(id: Long, strategy: String)
}
