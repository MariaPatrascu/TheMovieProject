package com.example.themovie.network.api.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.themovie.network.api.data.Movie

@Dao
interface MovieDao {
    @Query("SELECT * FROM Movie WHERE movieId = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?
    @Query("SELECT * FROM Movie WHERE isFavoriteMovie = :isFavoriteMovie")
    suspend fun getFavoriteMovies(isFavoriteMovie: Boolean = true): List<Movie>
    @Query("UPDATE Movie SET isFavoriteMovie=:isFavoriteMovie WHERE movieId = :movieId")
    suspend fun setFavoriteMovie(isFavoriteMovie: Boolean, movieId: Int)
    @Query("SELECT * FROM movie WHERE recommendationType = :recommendationType Order By releaseDate ASC")
    suspend fun moviesSortedByDateAscending(recommendationType: String): List<Movie>

    @Query("SELECT * FROM movie WHERE recommendationType = :recommendationType Order By releaseDate DESC")
    suspend fun moviesSortedByDateDescending(recommendationType: String): List<Movie>

    @Query("SELECT * FROM movie WHERE recommendationType = :recommendationType Order By voteAverage ASC")
    suspend fun moviesSortedByRatingAscending(recommendationType: String): List<Movie>

    @Query("SELECT * FROM movie WHERE recommendationType = :recommendationType Order By voteAverage DESC")
    suspend fun moviesSortedByRatingDescending(recommendationType: String): List<Movie>

    @Query("SELECT * FROM Movie WHERE recommendationType=:recommendationType")
    suspend fun getMoviesByRecommendation(recommendationType: String): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Query("SELECT COUNT() FROM Movie WHERE movieId = :movieId AND recommendationType=:recommendationType")
    suspend fun countDuplicatedMovies(movieId: Int, recommendationType: String?): Int
}
