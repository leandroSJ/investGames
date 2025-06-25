package com.example.investgames

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val cpf: String,
    val name: String,
    val email: String,
    val password: String
)