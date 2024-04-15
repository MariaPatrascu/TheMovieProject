package com.example.themovie.di

import com.example.themovie.network.api.IMovieApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiServiceModule {
    @Provides
    @Singleton
    fun providesMovieApiService(retrofit: Retrofit): IMovieApiService =
        retrofit.create(IMovieApiService::class.java)
}
