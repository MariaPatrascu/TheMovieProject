package com.example.themovie.network.error

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import javax.inject.Inject

class ErrorInterceptor @Inject constructor(private val gson: Gson) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val body = response.body
        return if (!response.isSuccessful && body != null) {
            val clonedResponseBody = cloneResponseBody(response)
            val content = clonedResponseBody.string()
            try {
                if (response.code == 503 || response.code == 504) {
                    throw gson.fromJson(content, ServiceUnavailableException::class.java)
                } else {
                    val apiException = gson.fromJson(content, ApiException::class.java)
                    if (apiException != null) {
                        throw apiException
                    } else {
                        response
                    }
                }
            } catch (e: JsonSyntaxException) {
                response
            }
        } else {
            response
        }
    }

    @Throws(IOException::class)
    private fun cloneResponseBody(rawResponse: Response): ResponseBody {
        return rawResponse.peekBody(Long.MAX_VALUE)
    }
}
