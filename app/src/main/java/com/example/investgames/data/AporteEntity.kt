package com.example.investgames

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aportes")
data class AporteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val valor: Float,
    val data: Long = System.currentTimeMillis()
)
