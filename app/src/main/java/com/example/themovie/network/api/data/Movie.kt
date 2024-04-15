package com.example.themovie.network.api.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Movie")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val typeId: Int,
    @SerializedName("id")
    @ColumnInfo("movieId")
    val movieId: Int,
    @ColumnInfo("title")
    val title: String,
    @SerializedName("poster_path")
    @ColumnInfo("posterPath")
    val posterPath: String?,
    @SerializedName("release_date")
    @ColumnInfo("releaseDate")
    val releaseDate: String,
    @SerializedName("vote_average")
    @ColumnInfo("voteAverage")
    val voteAverage: Double,
    @SerializedName("vote_count")
    @ColumnInfo("voteCount")
    val voteCount: Int,
    @SerializedName("backdrop_path")
    @ColumnInfo("backdropPath")
    val backdropPath: String?,
    @ColumnInfo("overview")
    val overview: String,
    @ColumnInfo("tagline")
    val tagline: String?,
    @ColumnInfo("genres")
    val genres: List<Genre>?,

    //local
    @ColumnInfo("recommendationType")
    var recommendationType: String?,
    @ColumnInfo("isFavoriteMovie")
    var isFavoriteMovie: Boolean?
)
