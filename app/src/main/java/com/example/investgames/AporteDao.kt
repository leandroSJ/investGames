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

    @Query("DELETE FROM aportes")
    suspend fun deleteTodosAportes()

    @Query("SELECT * FROM aportes ORDER BY data DESC")
    fun getTodosAportes(): Flow<List<AporteEntity>>


}
