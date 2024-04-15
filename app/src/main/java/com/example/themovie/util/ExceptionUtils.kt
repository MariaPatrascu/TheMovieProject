package com.example.themovie.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.themovie.R
import com.example.themovie.network.error.ApiException
import com.example.themovie.network.error.ServiceUnavailableException
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.URI
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


object ExceptionUtils {
    fun toast(message: String, context: Context?, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, length).show()
    }

    fun isInternetAvailable(context: Context): Boolean {
        val result: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        return result
    }

    fun getUserFacingErrorMessage(context: Context, error: Throwable): String {
        val msg: String = when (error) {
            is ServiceUnavailableException -> {
                error.statusMessage
            }

            is ApiException -> {
                error.statusMessage
            }

            else -> {
                error.message
            }
        } ?: context.resources.getString(R.string.default_error)

        return msg
    }

    private fun isUiHandleableException(exception: Throwable): Boolean {
        return exception is IOException
    }

    fun uiExceptionHandler(handler: (CoroutineContext, Throwable) -> Unit): CoroutineExceptionHandler =
        object : AbstractCoroutineContextElement(CoroutineExceptionHandler),
            CoroutineExceptionHandler {
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                val handleable = isUiHandleableException(exception)

                if (!handleable) {
                    if (exception is HttpException) {
                        logHttpException(exception)
                    }
                    throw exception
                }

                handler.invoke(context, exception)
            }

            private fun logHttpException(e: HttpException) {
                try {
                    val uri = URI(e.response()?.raw()?.request?.url?.toString())
                    val url = uri.scheme + "://" + uri.authority + uri.path
                    Timber.e(
                        Exception(
                            "HttpException: " + e.message() + " / url = " + url
                        )
                    )
                } catch (ex: Exception) {
                    Timber.e("Error while trying to parse/log HttpException request url")
                    Timber.e(ex)
                }
            }
        }
}
