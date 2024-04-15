package com.example.themovie.feature.common

import kotlinx.coroutines.flow.MutableStateFlow

enum class SortingOptions {
    RATING_ASCENDING,
    RATING_DESCENDING,
    DATE_ASCENDING,
    DATE_DESCENDING,
    DEFAULT
}

object SortingOptionsByRecommendation {
    val sortingOptionsNowPlaying: MutableStateFlow<SortingOptions> =
        MutableStateFlow(SortingOptions.DEFAULT)
    val sortingOptionsPopular: MutableStateFlow<SortingOptions> =
        MutableStateFlow(SortingOptions.DEFAULT)
    val sortingOptionsTopRated: MutableStateFlow<SortingOptions> =
        MutableStateFlow(SortingOptions.DEFAULT)
    val sortingOptionsUpcoming: MutableStateFlow<SortingOptions> =
        MutableStateFlow(SortingOptions.DEFAULT)
}
