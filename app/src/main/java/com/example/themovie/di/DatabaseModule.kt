package com.example.themovie.di

import android.content.Context
import androidx.room.Room
import com.example.themovie.network.api.data.room.MovieDao
import com.example.themovie.network.api.data.room.MovieRepository
import com.example.themovie.network.api.data.room.MovieRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideMovieDao(movieRoomDatabase: MovieRoomDatabase): MovieDao =
        movieRoomDatabase.movieDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MovieRoomDatabase =
        Room.databaseBuilder(
            appContext,
            MovieRoomDatabase::class.java,
            "MovieRoomDatabase"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideMovieRepository(movieDao: MovieDao): MovieRepository = MovieRepository(movieDao)
}
