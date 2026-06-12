package com.example.stayfree.data.repository

import com.example.stayfree.data.local.entity.InAppBlockEntity
import kotlinx.coroutines.flow.Flow

interface InAppBlockRepository {
    fun getAll(): Flow<List<InAppBlockEntity>>
    suspend fun getActiveOnce(): List<InAppBlockEntity>
    suspend fun getActiveForPackage(pkg: String): List<InAppBlockEntity>
    suspend fun insert(entity: InAppBlockEntity): Long
    suspend fun update(entity: InAppBlockEntity)
    suspend fun setActive(id: Long, active: Boolean)
}
