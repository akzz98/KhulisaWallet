package com.example.khulisawallet.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

//Ensure email is unique
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    //Auto generate the PK, ID
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val profileImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long? = null,
    val isActive: Boolean = true
)