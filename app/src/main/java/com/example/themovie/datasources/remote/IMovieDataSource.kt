package com.example.themovie.datasources.remote

import com.example.themovie.network.api.data.Movie

interface IMovieDataSource {
    suspend fun getMoviesByRecommendation(recommendationType: String): List<Movie>
    suspend fun search(searchQuery: String): List<Movie>
    suspend fun getMovieDetails(movieId: Int): Movie
}
