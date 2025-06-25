package com.example.investgames

import androidx.room.*
@Dao
interface UserDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): User?

    @Query("SELECT * FROM users WHERE (email = :login OR cpf = :login) AND password = :password")
    suspend fun login(login: String, password: String): User?

}