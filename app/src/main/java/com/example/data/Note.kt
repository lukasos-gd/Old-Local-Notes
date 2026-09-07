package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val colorHex: String = "#242735"
)
