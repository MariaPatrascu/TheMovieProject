package com.example.themovie.datasources.remote

import com.example.themovie.config.GlobalConfig
import com.example.themovie.network.api.IMovieApiService
import com.example.themovie.network.api.data.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieRemoteDataSource @Inject constructor(private val movieApiService: IMovieApiService) :
    IMovieDataSource {
    override suspend fun getMoviesByRecommendation(recommendationType: String): List<Movie> {
        val response = withContext(Dispatchers.IO) {
            movieApiService.getMoviesByRecommendation(
                GlobalConfig.apiBaseUrl,
                recommendationType,
                GlobalConfig.apiKey
            )
        }
        return response.run {
            this.results
        }
    }

    override suspend fun search(searchQuery: String): List<Movie> {
        val response = withContext(Dispatchers.IO) {
            movieApiService.search(
                GlobalConfig.apiBaseUrl,
                GlobalConfig.apiKey,
                searchQuery
            )
        }
        return response.run {
            this.results
        }
    }

    override suspend fun getMovieDetails(movieId: Int): Movie {
        val response = withContext(Dispatchers.IO) {
            movieApiService.getMovieDetails(
                GlobalConfig.apiBaseUrl,
                movieId,
                GlobalConfig.apiKey
            )
        }
        return response.run {
            this
        }
    }
}
