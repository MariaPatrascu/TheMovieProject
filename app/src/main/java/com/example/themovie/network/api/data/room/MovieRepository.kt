package com.example.themovie.network.api.data.room

import com.example.themovie.network.api.data.Movie
import javax.inject.Inject

class MovieRepository @Inject constructor(private val movieDao: MovieDao) {
    suspend fun movieById(movieId: Int): Movie? = movieDao.getMovieById(movieId)
    suspend fun favoriteMovies(): List<Movie> = movieDao.getFavoriteMovies()
    suspend fun setFavoriteMovie(isFavoriteMovie: Boolean, movieId: Int) =
        movieDao.setFavoriteMovie(isFavoriteMovie, movieId)

    suspend fun moviesByRecommendation(recommendationString: String): List<Movie> =
        movieDao.getMoviesByRecommendation(recommendationString)

    suspend fun moviesSortedByDateAscending(recommendationType: String): List<Movie> =
        movieDao.moviesSortedByDateAscending(recommendationType)

    suspend fun moviesSortedByDateDescending(recommendationType: String): List<Movie> =
        movieDao.moviesSortedByDateDescending(recommendationType)

    suspend fun moviesSortedByRatingAscending(recommendationType: String): List<Movie> =
        movieDao.moviesSortedByRatingAscending(recommendationType)

    suspend fun moviesSortedByRatingDescending(recommendationType: String): List<Movie> =
        movieDao.moviesSortedByRatingDescending(recommendationType)

    suspend fun insertMovies(movies: List<Movie>) =
        movieDao.insertMovies(movies)

    suspend fun countDuplicatedMovies(movieId: Int, recommendationType: String?) =
        movieDao.countDuplicatedMovies(movieId, recommendationType)
}
