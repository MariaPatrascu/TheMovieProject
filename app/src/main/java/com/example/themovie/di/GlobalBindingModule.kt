package com.example.themovie.di

import com.example.themovie.feature.common.MainActivity
import com.example.themovie.feature.movie.details.MovieDetailsActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GlobalBindingModule {
    @Binds
    abstract fun homeActivity(mainActivity: MainActivity): MainActivity

    @Binds
    abstract fun movieDetailsActivity(movieDetailsActivity: MovieDetailsActivity): MovieDetailsActivity
}
