package com.example.themovie.datasources.repository

import com.example.themovie.datasources.remote.MovieRemoteDataSource
import com.example.themovie.network.api.data.Movie
import javax.inject.Inject

class MovieRepository @Inject constructor(private val movieRemoteDataSource: MovieRemoteDataSource) {
    suspend fun getMoviesByRecommendation(recommendationType: String): List<Movie> =
        movieRemoteDataSource.getMoviesByRecommendation(recommendationType)

    suspend fun search(searchQuery: String): List<Movie> = movieRemoteDataSource.search(searchQuery)

    suspend fun getMovieDetails(movieId: Int): Movie =
        movieRemoteDataSource.getMovieDetails(movieId)
}
