package com.example.themovie.feature.home.pager.top_rated

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.themovie.R
import com.example.themovie.domain.usecase.GetMovies
import com.example.themovie.feature.common.SortingOptions
import com.example.themovie.feature.home.pager.MoviePager.Change
import com.example.themovie.feature.home.pager.MoviePager.Intent
import com.example.themovie.feature.home.pager.MoviePager.Reducer
import com.example.themovie.feature.home.pager.MoviePager.State
import com.example.themovie.feature.search.Search
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
class TopRatedViewModel @Inject constructor(
    override val reducer: Reducer,
    private val getMoviesByRecommendation: GetMovies,
    private val movieViewModel: MovieViewModel
) : ReactiveViewModel<State, Intent, Change, NavDirection>(
    State.INITIAL
) {
    private var getMoviesDataJob: Job? = null
    private var movieAdapterPosition: Int = 0
    override suspend fun process(
        intent: Intent,
        state: State
    ) = when (intent) {
        is Intent.ShowMoviesByRecommendation -> loadData(
            intent.recommendationType
        )

        is Intent.SortMovies -> sortMovies(
            intent.sortingOption,
            intent.recommendationType
        )

        is Intent.OnMovieSelection -> {
            movieAdapterPosition = intent.movieAdapterPosition
            goToMovieDetails(intent.sortingOption, intent.recommendationType, intent.movieId)
        }

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

        is Intent.RefreshContent -> refreshData(
            intent.context,
            intent.recommendationType,
            intent.sortingOption
        )

        is Intent.ResetAdapterPosition -> {
            movieAdapterPosition = 0
            change(Change.ResetAdapterPosition(0))
        }
    }

    private suspend fun refreshData(
        context: Context,
        recommendationType: String,
        sortingOption: String
    ) {
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
            val moviesForDatabase = mutableListOf<Movie>()
            val movieList = getMoviesByRecommendation.invoke(recommendationType)
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
            sortMovies(sortingOption, recommendationType)
        }
    }

    private suspend fun goToMovieDetails(
        sortingOption: String,
        recommendationType: String,
        movieId: Int
    ) {
        navigate(
            Search.Navigation.GoToMovieDetailsActivity(
                movieId
            ) {
                viewModelScope.launch {
                    when (it) {
                        true -> sortMovies(sortingOption, recommendationType)
                        false -> {}
                    }
                }
            })
    }

    // sort the movies based on the user selection from the toolbar menu options filtered by recommendation type selected
    // in this case "top_rated"
    private suspend fun sortMovies(sortingOption: String, recommendationType: String) {
        var newMovieList: List<Movie> = emptyList()
        when (sortingOption) {
            SortingOptions.RATING_ASCENDING.name ->
                newMovieList = movieViewModel.moviesSortedByRatingAscending(recommendationType)

            SortingOptions.RATING_DESCENDING.name ->
                newMovieList = movieViewModel.moviesSortedByRatingDescending(recommendationType)

            SortingOptions.DATE_ASCENDING.name ->
                newMovieList = movieViewModel.moviesSortedByDateAscending(recommendationType)

            SortingOptions.DATE_DESCENDING.name ->
                newMovieList = movieViewModel.moviesSortedByDateDescending(recommendationType)

            SortingOptions.DEFAULT.name -> newMovieList = movieViewModel.moviesByRecommendation(
                recommendationType
            )
        }
        change(Change.SetContent(movieAdapterPosition, newMovieList))
    }

    // show the data from the database filtered by recommendation type
    // in this case "top_rated"
    private suspend fun loadData(recommendationType: String) =
        change(
            Change.SetContent(
                movieAdapterPosition = movieAdapterPosition,
                movieList = movieViewModel.moviesByRecommendation(
                    recommendationType
                )
            )
        )

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
