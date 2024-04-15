package com.example.themovie.domain.usecase

import com.example.themovie.datasources.repository.MovieRepository
import com.example.themovie.network.api.data.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMovies @Inject constructor(private val movieRepository: MovieRepository) {
    suspend operator fun invoke(recommendationString: String): List<Movie> =
        movieRepository.getMoviesByRecommendation(recommendationString)
}
