package com.example.investgames.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val valor: Float,
    val objetivo: String
)