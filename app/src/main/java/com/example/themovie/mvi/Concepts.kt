package com.example.themovie.mvi

/**
 * The entire state of a given screen.
 */
interface UiState

/**
 * The user's intention of performing an action
 */
interface UiIntent

/**
 * The action of updating the current state towards creating a new state
 */
interface UiChange

/**
 * Navigation Direction.
 */
interface NavDirection

/**
 * A Reducer is responsible for mutating a given State by applying a Change, thus generating a new
 * State.
 */
interface UiReducer<S : UiState, in C : UiChange> {
    operator fun invoke(state: S, change: C): S
}
