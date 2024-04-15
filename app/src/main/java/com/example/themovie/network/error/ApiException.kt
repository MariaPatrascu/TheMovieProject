package com.example.themovie.network.error

import com.google.gson.annotations.SerializedName
import java.io.IOException

open class ApiException(
    @SerializedName("status_message")
    private val status: String? = ""
) : IOException() {
    val statusMessage: String?
        get() {
            var msg = status
            if (msg.isNullOrEmpty()) {
                msg = message
            }
            return msg
        }
}
