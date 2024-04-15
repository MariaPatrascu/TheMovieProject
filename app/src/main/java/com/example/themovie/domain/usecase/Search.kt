package com.example.themovie.domain.usecase

import com.example.themovie.datasources.repository.MovieRepository
import com.example.themovie.network.api.data.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Search @Inject constructor(private val movieRepository: MovieRepository) {
    suspend operator fun invoke(searchQuery: String): List<Movie> =
        movieRepository.search(searchQuery)
}
