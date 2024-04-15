package com.example.themovie.network.api

import com.example.themovie.network.api.data.Movie
import com.example.themovie.network.api.data.MovieList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IMovieApiService {
    //get movies by recommendation type = { now_playing, popular, top_rated, upcoming }
    //returns an array of Movie
    @GET("{baseRxUrl}/movie/{recommendation_type}")
    suspend fun getMoviesByRecommendation(
        @Path(value = "baseRxUrl", encoded = true) baseRxUrl: String,
        @Path(value = "recommendation_type") recommendationType: String,
        @Query("api_key") apiKey: String
    ): MovieList

    //search - pass a text query to search of min length 1
    //returns an array of Movie
    @GET("{baseRxUrl}/search/movie")
    suspend fun search(
        @Path(value = "baseRxUrl", encoded = true) baseRxUrl: String,
        @Query("api_key") apiKey: String,
        @Query("query") searchQuery: String
    ): MovieList

    //movie details
    //returns Movie
    @GET("{baseRxUrl}/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path(value = "baseRxUrl", encoded = true) baseRxUrl: String,
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Movie
}