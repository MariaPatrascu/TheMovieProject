package com.example.themovie.feature.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.themovie.R
import com.example.themovie.domain.usecase.GetMovies
import com.example.themovie.feature.home.Home.Change
import com.example.themovie.feature.home.Home.Intent
import com.example.themovie.feature.home.Home.Reducer
import com.example.themovie.feature.home.Home.State
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveViewModel
import com.example.themovie.network.api.data.Movie
import com.example.themovie.network.api.data.room.MovieViewModel
import com.example.themovie.util.ExceptionUtils
import com.example.themovie.util.ExceptionUtils.uiExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    override val reducer: Reducer,
    private val getMoviesByRecommendation: GetMovies,
    private val movieViewModel: MovieViewModel
) : ReactiveViewModel<State, Intent, Change, NavDirection>(
    State.INITIAL
) {
    private var getMoviesDataJob: Job? = null

    override suspend fun process(intent: Intent, state: State) = when (intent) {
        is Intent.LoadContent -> loadData(intent.context, intent.recommendationTypes)
        is Intent.RecommendationSelectedPosition -> navigate(
            Home.Navigation.RecommendationSelectedPosition(
                intent.position
            )
        )
    }

    private suspend fun loadData(context: Context, recommendationTypes: List<String>) {
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
        getMoviesDataJob?.cancel()
        getMoviesDataJob = viewModelScope.launch(getErrorHandler(context)) {
            recommendationTypes.forEach { recommendationType ->
                // insert the movies into the database if they don't exist and prepare the data
                // to show inside of our view pager so we don't make the download every time
                val moviesForDatabase = mutableListOf<Movie>()
                getMoviesByRecommendation.invoke(recommendationType).forEach {
                    it.recommendationType = recommendationType
                    if (movieViewModel.countDuplicatedMovies(
                            it.movieId,
                            recommendationType
                        ) == 0
                    ) moviesForDatabase.add(it)
                }
                movieViewModel.insertMovies(moviesForDatabase)
            }
            change(Change.SetDataLoaded(true))
        }
    }

    // show api error
    private fun getErrorHandler(context: Context) = uiExceptionHandler { _, e ->
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
