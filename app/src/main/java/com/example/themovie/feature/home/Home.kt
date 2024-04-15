package com.example.themovie.feature.home

import android.content.Context
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.UiChange
import com.example.themovie.mvi.UiIntent
import com.example.themovie.mvi.UiReducer
import com.example.themovie.mvi.UiState
import com.example.themovie.util.SingleAccessData
import javax.inject.Inject

interface Home {
    data class State(
        val isLoading: Boolean = false,
        val dataLoaded: SingleAccessData<Boolean?> = SingleAccessData(null, isConsumed = true),
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
        data class LoadContent(val context: Context, val recommendationTypes: List<String>) :
            Intent()

        data class RecommendationSelectedPosition(val position: Int = 0) : Intent()
    }

    sealed class Change : UiChange {
        data class SetLoading(val isLoading: Boolean = false) : Change()
        data class SetDataLoaded(val dataLoaded: Boolean) : Change()
        data class SetError(val error: State.Error?) : Change()
    }

    sealed class Navigation : NavDirection {
        data class RecommendationSelectedPosition(val position: Int) : Navigation()
    }

    class Reducer @Inject constructor() : UiReducer<State, Change> {
        override fun invoke(state: State, change: Change): State {
            return when (change) {
                is Change.SetLoading -> state.copy(isLoading = change.isLoading)
                is Change.SetDataLoaded -> state.copy(
                    dataLoaded = SingleAccessData(change.dataLoaded),
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
