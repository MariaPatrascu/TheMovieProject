package com.example.themovie.feature.movie.details

import android.content.Context
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.UiChange
import com.example.themovie.mvi.UiIntent
import com.example.themovie.mvi.UiReducer
import com.example.themovie.mvi.UiState
import com.example.themovie.network.api.data.Movie
import com.example.themovie.util.SingleAccessData
import javax.inject.Inject

interface MovieDetails {
    data class State(
        val isLoading: Boolean = false,
        val movie: SingleAccessData<Movie?> = SingleAccessData(null, isConsumed = true),
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
        data class LoadContent(val context: Context, val movieId: Int) : Intent()
        data object OnCloseClicked : Intent()
        data class OnAddToFavoritesClicked(
            val context: Context,
            val movieId: Int
        ) : Intent()

        data class OnRemoveFromFavoritesClicked(
            val context: Context,
            val movieId: Int
        ) : Intent()
    }

    sealed class Change : UiChange {
        data class SetLoading(val isLoading: Boolean) : Change()
        data class SetContent(val movie: Movie) : Change()
        data class SetError(val error: State.Error?) : Change()
    }

    sealed class Navigation : NavDirection {
        data class GoBack(val movieIsEdited: Boolean) : Navigation()
    }

    class Reducer @Inject constructor() : UiReducer<State, Change> {
        override fun invoke(state: State, change: Change): State {
            return when (change) {
                is Change.SetLoading -> state.copy(isLoading = change.isLoading)
                is Change.SetContent -> state.copy(
                    movie = SingleAccessData(change.movie),
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
