package com.example.investgames

import androidx.room.*
import com.example.investgames.data.MetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AporteDao {
    @Insert
    suspend fun inserirAporte(aporte: AporteEntity)

    @Query("SELECT SUM(valor) FROM aportes")
    suspend fun obterTotalAportado(): Float?
}
