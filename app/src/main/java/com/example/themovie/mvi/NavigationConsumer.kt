package com.example.themovie.mvi

/**
 * NavigationConsumer needs to provide a way to consume a navigation direct. The container Activity needs to
 * implement this.
 */
interface NavigationConsumer<N : NavDirection> {
    fun navigate(navDirection: NavDirection)
}
