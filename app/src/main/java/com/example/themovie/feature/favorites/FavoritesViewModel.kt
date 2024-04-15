package com.example.themovie.feature.favorites

import androidx.lifecycle.viewModelScope
import com.example.themovie.feature.favorites.Favorites.Change
import com.example.themovie.feature.favorites.Favorites.Intent
import com.example.themovie.feature.favorites.Favorites.Reducer
import com.example.themovie.feature.favorites.Favorites.State
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveViewModel
import com.example.themovie.network.api.data.room.MovieViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    override val reducer: Reducer,
    private val movieViewModel: MovieViewModel
) : ReactiveViewModel<State, Intent, Change, NavDirection>(
    State.INITIAL
) {
    override suspend fun process(
        intent: Intent,
        state: State
    ) = when (intent) {
        is Intent.OnMovieSelection -> goToMovieDetails(intent.movieId)

        is Intent.OnAddToFavoritesClicked -> {
            movieViewModel.setFavoriteMovie(
                true,
                intent.movieId
            )
        }

        is Intent.OnRemoveFromFavoritesClicked -> {
            movieViewModel.setFavoriteMovie(
                false,
                intent.movieId
            )
            loadData()
        }

        is Intent.LoadContent -> loadData()
    }

    private suspend fun goToMovieDetails(movieId: Int) {
        navigate(
            Favorites.Navigation.GoToMovieDetailsActivity(
                movieId
            ) {
                viewModelScope.launch {
                    when (it) {
                        true -> loadData()
                        false -> {}
                    }
                }
            })
    }

    // load unique favorite movies from the database
    private suspend fun loadData() =
        change(
            Change.SetContent(
                movieList = movieViewModel.favoriteMovies().distinctBy { it.movieId }
            )
        )
}
