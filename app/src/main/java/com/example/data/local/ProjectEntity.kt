package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectName: String,
    val buildingTypeName: String,
    val constructionTypeName: String,
    val floors: Int,
    val landAreaSqm: Double,
    val totalBuiltAreaSqm: Double,
    val totalCost: Double,
    val currencyCode: String,
    val selectedDesignId: String,
    val payloadJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
