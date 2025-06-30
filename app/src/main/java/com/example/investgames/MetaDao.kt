package com.example.investgames

import androidx.room.*
import com.example.investgames.data.MetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeta(meta: MetaEntity)

    @Query("SELECT * FROM metas ORDER BY id DESC LIMIT 1")
    fun getMetaAtual(): Flow<MetaEntity?>

    @Query("DELETE FROM metas")
    suspend fun deleteTodasMetas()

    @Query("SELECT * FROM metas ORDER BY id DESC LIMIT 1")
    suspend fun getMetaAtualSimples(): MetaEntity?
}
