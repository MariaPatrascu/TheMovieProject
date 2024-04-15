package com.example.themovie.feature.favorites

import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.UiChange
import com.example.themovie.mvi.UiIntent
import com.example.themovie.mvi.UiReducer
import com.example.themovie.mvi.UiState
import com.example.themovie.network.api.data.Movie
import com.example.themovie.util.SingleAccessData
import javax.inject.Inject

interface Favorites {
    data class State(
        val movieList: SingleAccessData<List<Movie>?> = SingleAccessData(null, isConsumed = true)
    ) : UiState {
        companion object {
            val INITIAL = State()
        }
    }

    sealed class Intent : UiIntent {
        data object LoadContent : Intent()
        data class OnMovieSelection(val movieId: Int) : Intent()
        data class OnAddToFavoritesClicked(
            val movieId: Int
        ) : Intent()

        data class OnRemoveFromFavoritesClicked(
            val movieId: Int
        ) : Intent()
    }

    sealed class Change : UiChange {
        data class SetContent(val movieList: List<Movie>) : Change()
    }

    sealed class Navigation : NavDirection {
        data class GoToMovieDetailsActivity(
            val movieId: Int,
            val resultCallback: ((Boolean) -> Unit)
        ) : NavDirection
    }

    class Reducer @Inject constructor() : UiReducer<State, Change> {
        override fun invoke(state: State, change: Change): State {
            return when (change) {
                is Change.SetContent -> state.copy(
                    movieList = SingleAccessData(change.movieList)
                )
            }
        }
    }
}
