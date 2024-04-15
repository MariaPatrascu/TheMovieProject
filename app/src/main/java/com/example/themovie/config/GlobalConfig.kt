package com.example.themovie.config

object GlobalConfig {
    //display movie image base url
    val baseImageUrl: String
        get() = "https://image.tmdb.org/t/p/w500"
    //api key param request
    val apiKey: String
        get() = "abfabb9de9dc58bb436d38f97ce882bc"
    //get data base url
    val apiBaseUrl: String
        get() = "https://api.themoviedb.org/3"
}
