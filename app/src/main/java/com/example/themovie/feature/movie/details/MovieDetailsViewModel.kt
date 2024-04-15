package com.example.themovie.feature.movie.details

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.themovie.R
import com.example.themovie.domain.usecase.GetMovieDetails
import com.example.themovie.feature.movie.details.MovieDetails.Change
import com.example.themovie.feature.movie.details.MovieDetails.Intent
import com.example.themovie.feature.movie.details.MovieDetails.Reducer
import com.example.themovie.feature.movie.details.MovieDetails.State
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveViewModel
import com.example.themovie.network.api.data.room.MovieViewModel
import com.example.themovie.util.ExceptionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    override val reducer: Reducer,
    private val movieViewModel: MovieViewModel,
    private val getMovieDetails: GetMovieDetails
) : ReactiveViewModel<State, Intent, Change, NavDirection>(
    State.INITIAL
) {
    private var getMovieByIdDataJob: Job? = null
    private var movieIsEdited = false
    override suspend fun process(intent: Intent, state: State) =
        when (intent) {
            is Intent.LoadContent -> loadData(intent.context, intent.movieId)
            is Intent.OnCloseClicked -> navigate(MovieDetails.Navigation.GoBack(movieIsEdited))
            is Intent.OnAddToFavoritesClicked -> {
                movieViewModel.setFavoriteMovie(
                    true,
                    intent.movieId
                )
                movieIsEdited = true
                loadData(intent.context, intent.movieId)
            }

            is Intent.OnRemoveFromFavoritesClicked -> {
                movieViewModel.setFavoriteMovie(
                    false,
                    intent.movieId
                )
                movieIsEdited = true
                loadData(intent.context, intent.movieId)
            }
        }

    private suspend fun loadData(context: Context, movieId: Int) {
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
        getMovieByIdDataJob?.cancel()
        getMovieByIdDataJob = viewModelScope.launch(getErrorHandler(context)) {
            val movie = getMovieDetails.invoke(movieId)
            // getting the movie from the database to check if it's a favorite movie due to the fact that we
            // store this locally and the "/movie/{movie_id}" endpoint has some additional fields that we need like "tagline",
            // "genres", "overview"
            val movieFromDatabase = movieViewModel.movieById(movieId)
            if (movieFromDatabase?.isFavoriteMovie != null &&
                movieFromDatabase.isFavoriteMovie == true
            ) movie.isFavoriteMovie = true
            change(Change.SetContent(movie))
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
