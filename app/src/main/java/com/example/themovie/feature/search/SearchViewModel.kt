package com.example.themovie.feature.search

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.themovie.R
import com.example.themovie.domain.usecase.Search
import com.example.themovie.feature.search.Search.Change
import com.example.themovie.feature.search.Search.Intent
import com.example.themovie.feature.search.Search.Navigation
import com.example.themovie.feature.search.Search.Reducer
import com.example.themovie.feature.search.Search.State
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveViewModel
import com.example.themovie.network.api.data.Movie
import com.example.themovie.network.api.data.room.MovieViewModel
import com.example.themovie.util.ExceptionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    override val reducer: Reducer,
    private val searchForMovies: Search,
    private val movieViewModel: MovieViewModel
) : ReactiveViewModel<State, Intent, Change, NavDirection>(
    State.INITIAL
) {
    private var searchDataJob: Job? = null
    override suspend fun process(intent: Intent, state: State) =
        when (intent) {
            is Intent.SearchForMovie -> loadData(intent.context, intent.searchQuery)
            is Intent.OnMovieSelection -> goToMovieDetails(
                intent.context,
                intent.movieId,
                intent.searchQuery
            )

            is Intent.OnAddToFavoritesClicked ->
                movieViewModel.setFavoriteMovie(
                    true,
                    intent.movieId
                )

            is Intent.OnRemoveFromFavoritesClicked ->
                movieViewModel.setFavoriteMovie(
                    false,
                    intent.movieId
                )
        }

    private suspend fun goToMovieDetails(context: Context, movieId: Int, searchQuery: String) {
        navigate(
            Navigation.GoToMovieDetailsActivity(
                movieId
            ) {
                viewModelScope.launch {
                    when (it) {
                        true -> loadData(context, searchQuery)
                        false -> {}
                    }
                }
            })
    }

    private suspend fun loadData(context: Context, searchQuery: String) {
        // show no internet connection error
        val showInternetOffline = !ExceptionUtils.isInternetAvailable(context)
        if (showInternetOffline) {
            val message = context.getString(R.string.offline)
            change(
                Change.SetError(State.Error.Network(message))
            )
            return
        }

        change(Change.SetLoading(true))
        searchDataJob?.cancel()
        searchDataJob = viewModelScope.launch(getErrorHandler(context)) {
            val moviesForDatabase = mutableListOf<Movie>()
            val movieList = searchForMovies.invoke(searchQuery).filter { it.posterPath != null }
            movieList.forEach {
                // check for favorite movies to update the icon when getting back from
                // movie details screen
                val movieFromDatabase = movieViewModel.movieById(it.movieId)
                if (movieFromDatabase?.isFavoriteMovie != null &&
                    movieFromDatabase.isFavoriteMovie == true
                ) {
                    it.isFavoriteMovie = true
                }

                // check if movie it's already present inside the database
                // if not, we add it for the favorite icon update when we come
                // back from the movie details screen
                if (movieViewModel.countDuplicatedMovies(
                        it.movieId,
                        null
                    ) == 0
                ) moviesForDatabase.add(it)
            }
            movieViewModel.insertMovies(moviesForDatabase)
            change(Change.SetContent(movieList))
        }
    }

    // show api error
    private fun getErrorHandler(context: Context) = ExceptionUtils.uiExceptionHandler { _, e ->
        viewModelScope.launch {
            val msg = ExceptionUtils.getUserFacingErrorMessage(context, e)
            change(
                Change.SetError(
                    State.Error.Default(msg)
                )
            )
        }
    }
}
