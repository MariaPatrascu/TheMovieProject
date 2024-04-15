package com.example.themovie.feature.home.pager

import android.content.Context
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.UiChange
import com.example.themovie.mvi.UiIntent
import com.example.themovie.mvi.UiReducer
import com.example.themovie.mvi.UiState
import com.example.themovie.network.api.data.Movie
import com.example.themovie.util.SingleAccessData
import javax.inject.Inject

interface MoviePager {
    data class State(
        val isLoading: Boolean = false,
        val movieList: SingleAccessData<List<Movie>?> = SingleAccessData(null, isConsumed = true),
        val error: SingleAccessData<Error?> = SingleAccessData(null, isConsumed = true)
    ) : UiState {
        companion object {
            val INITIAL = State()
        }

        sealed class Error {
            data class Default(val message: String) : Error()
            data class Network(val message: String) : Error()
        }
    }

    sealed class Intent : UiIntent {
        data class RefreshContent(
            val context: Context,
            val recommendationType: String,
            val sortingOption: String
        ) : Intent()

        data class ShowMoviesByRecommendation(val recommendationType: String) : Intent()
        data class SortMovies(val sortingOption: String, val recommendationType: String) : Intent()
        data class OnMovieSelection(val recommendationType: String, val movieId: Int) : Intent()
        data class OnAddToFavoritesClicked(
            val movieId: Int,
            val recommendationType: String
        ) : Intent()

        data class OnRemoveFromFavoritesClicked(
            val movieId: Int,
            val recommendationType: String
        ) : Intent()
    }

    sealed class Change : UiChange {
        data class SetLoading(val isLoading: Boolean) : Change()
        data class SetContent(val movieList: List<Movie>) : Change()
        data class SetError(val error: State.Error?) : Change()
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
                is Change.SetLoading -> state.copy(isLoading = change.isLoading)
                is Change.SetContent -> state.copy(
                    movieList = SingleAccessData(change.movieList),
                    isLoading = false
                )

                is Change.SetError -> state.copy(
                    error = SingleAccessData(change.error),
                    isLoading = false
                )
            }
        }
    }
}
