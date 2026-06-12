package com.example.stayfree.data.local.db.dao

import androidx.room.*
import com.example.stayfree.data.local.entity.BlockRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockRuleDao {

    @Query("SELECT * FROM block_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE isActive = 1")
    fun getActiveRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE isActive = 1")
    suspend fun getActiveRulesOnce(): List<BlockRuleEntity>

    @Query("SELECT * FROM block_rules WHERE packageName = :pkg AND isActive = 1")
    suspend fun getActiveRulesForPackage(pkg: String): List<BlockRuleEntity>

    @Query("SELECT * FROM block_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): BlockRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: BlockRuleEntity): Long

    @Update
    suspend fun update(rule: BlockRuleEntity)

    @Delete
    suspend fun delete(rule: BlockRuleEntity)

    @Query("DELETE FROM block_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE block_rules SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE block_rules SET isActive = 0 WHERE packageName = :pkg")
    suspend fun deactivateAllForPackage(pkg: String)
}
