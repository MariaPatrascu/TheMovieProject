package com.example.themovie.network.error

class ServiceUnavailableException(
    serverErrorMessage: String
) : ApiException(serverErrorMessage)