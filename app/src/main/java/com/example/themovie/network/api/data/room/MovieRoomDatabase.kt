package com.example.themovie.network.api.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.themovie.network.api.data.Genre
import com.example.themovie.network.api.data.Movie

@Database(entities = [Movie::class, Genre::class], version = 20)
@TypeConverters(Converters::class)
abstract class MovieRoomDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
