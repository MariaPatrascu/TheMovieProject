package com.example.themovie.feature.search

import android.content.Context
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.UiChange
import com.example.themovie.mvi.UiIntent
import com.example.themovie.mvi.UiReducer
import com.example.themovie.mvi.UiState
import com.example.themovie.network.api.data.Movie
import com.example.themovie.util.SingleAccessData
import javax.inject.Inject

interface Search {
    data class State(
        val isLoading: Boolean = false,
        val movieList: SingleAccessData<List<Movie>?> = SingleAccessData(null, isConsumed = true),
        val error: SingleAccessData<Error?> = SingleAccessData(null, isConsumed = true),
        val movieAdapterPosition: SingleAccessData<Int?> = SingleAccessData(null, isConsumed = true)
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
        data class SearchForMovie(val context: Context, val searchQuery: String) : Intent()
        data class OnMovieSelection(
            val movieAdapterPosition: Int,
            val context: Context,
            val movieId: Int,
            val searchQuery: String
        ) : Intent()

        data class OnAddToFavoritesClicked(
            val movieId: Int
        ) : Intent()

        data class OnRemoveFromFavoritesClicked(
            val movieId: Int
        ) : Intent()

        data object ResetAdapterPosition : Intent()
    }

    sealed class Change : UiChange {
        data class SetLoading(val isLoading: Boolean) : Change()
        data class SetContent(val movieAdapterPosition: Int, val movieList: List<Movie>) : Change()
        data class SetError(val error: State.Error?) : Change()
        data class ResetAdapterPosition(val movieAdapterPosition: Int) : Change()
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
                    movieAdapterPosition = SingleAccessData(change.movieAdapterPosition),
                    movieList = SingleAccessData(change.movieList),
                    isLoading = false
                )

                is Change.SetError -> state.copy(
                    error = SingleAccessData(change.error),
                    isLoading = false
                )

                is Change.ResetAdapterPosition -> state.copy(
                    movieAdapterPosition = SingleAccessData(
                        change.movieAdapterPosition
                    )
                )
            }
        }
    }
}
