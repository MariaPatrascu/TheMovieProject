package com.example.themovie.network.api.data.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themovie.network.api.data.Movie
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieViewModel @Inject constructor(private val repository: MovieRepository) : ViewModel() {
    suspend fun movieById(movieId: Int): Movie? = repository.movieById(movieId)
    suspend fun favoriteMovies(): List<Movie> = repository.favoriteMovies()
    suspend fun setFavoriteMovie(isFavoriteMovie: Boolean, movieId: Int) =
        repository.setFavoriteMovie(isFavoriteMovie, movieId)

    suspend fun moviesSortedByDateAscending(recommendationString: String): List<Movie> =
        repository.moviesSortedByDateAscending(recommendationString)

    suspend fun moviesSortedByDateDescending(recommendationString: String): List<Movie> =
        repository.moviesSortedByDateDescending(recommendationString)

    suspend fun moviesSortedByRatingAscending(recommendationString: String): List<Movie> =
        repository.moviesSortedByRatingAscending(recommendationString)

    suspend fun moviesSortedByRatingDescending(recommendationString: String): List<Movie> =
        repository.moviesSortedByRatingDescending(recommendationString)

    suspend fun moviesByRecommendation(recommendationString: String): List<Movie> =
        repository.moviesByRecommendation(recommendationString)

    suspend fun insertMovies(movies: List<Movie>) = viewModelScope.launch {
        repository.insertMovies(movies)
    }

    suspend fun countDuplicatedMovies(movieId: Int, recommendationType: String?) =
        repository.countDuplicatedMovies(movieId, recommendationType)
}
