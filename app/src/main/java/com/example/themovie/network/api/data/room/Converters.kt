package com.example.themovie.network.api.data.room

import androidx.room.TypeConverter
import com.example.themovie.network.api.data.Genre
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun listToJson(value: List<Genre>?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value ?: "", Array<Genre>::class.java)?.toList()
}
