package com.example.stayfree.data.repository

import com.example.stayfree.data.local.db.dao.InAppBlockDao
import com.example.stayfree.data.local.entity.InAppBlockEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppBlockRepositoryImpl @Inject constructor(
    private val dao: InAppBlockDao
) : InAppBlockRepository {
    override fun getAll(): Flow<List<InAppBlockEntity>> = dao.getAll()
    override suspend fun getAllOnce(): List<InAppBlockEntity> = dao.getAllOnce()
    override suspend fun getActiveOnce(): List<InAppBlockEntity> = dao.getActiveOnce()
    override suspend fun getActiveForPackage(pkg: String): List<InAppBlockEntity> = dao.getActiveForPackage(pkg)
    override suspend fun insert(entity: InAppBlockEntity): Long = dao.insert(entity)
    override suspend fun update(entity: InAppBlockEntity) = dao.update(entity)
    override suspend fun setActive(id: Long, active: Boolean) = dao.setActive(id, active)
    override suspend fun updateStrategy(id: Long, strategy: String) = dao.updateStrategy(id, strategy)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
