package com.example.stayfree.domain.model

data class InAppBlock(
    val id: Long,
    val targetApp: String,
    val featureName: String,
    val detectionStrategy: String,  // JSON
    val isActive: Boolean,
    val syncId: String
)
