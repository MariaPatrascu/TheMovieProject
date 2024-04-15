package com.example.themovie.mvi

/**
 * StateRenderer needs to provide a way to consume (eg. display) the State. The View needs to
 * implement this.
 */
interface StateConsumer<S : UiState> {
    fun render(state: S)
}
