package com.example.themovie.network.api.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Genre")
data class Genre(
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo("genreId")
    val genreId: Int,
    @ColumnInfo("name")
    val name: String
)
