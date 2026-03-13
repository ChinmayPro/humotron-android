package com.humotron.app.data.network.error

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.humotron.app.R
import com.humotron.app.core.ErrorCode
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class ErrorUtils(val context: Context) {

    fun handleError(e: Exception?, key: String?): Error {

        return if (e != null) {

            when (e) {

                is IllegalStateException -> {
                    Error(ErrorCode.SERVER_ERROR, e.localizedMessage, key)
                }

                is SocketTimeoutException -> {
                    Error(
                        ErrorCode.SERVER_ERROR,
                        context.getString(R.string.time_out),
                        key
                    )
                }

                is ConnectException, is UnknownHostException -> {
                    Error(
                        ErrorCode.INTERNET_ERROR,
                        context.getString(R.string.no_internet),
                        key
                    )
                }

                is JsonParseException -> {
                    Error(
                        ErrorCode.SERVER_ERROR,
                        context.getString(R.string.something_went_wrong),
                        key
                    )
                }

                else -> {
                    handleMapError(e.message ?: "")
                    //                Error(AppConstant.Error.UNKNOWN_ERROR, e.message, key)
                }
            }

        } else Error(
            ErrorCode.UNKNOWN_ERROR,
            context.getString(R.string.unknown_error),
            key
        )

    }

    fun handleMapError(errorString: String): Error {
        return try {
            val map = Gson().fromJson(errorString, Map::class.java)

            val message = map?.get("error") ?: map?.get("message") ?: context.getString(R.string.something_went_wrong)

            Error(
                errorCode = ErrorCode.UNKNOWN_ERROR,
                errorMessage = message.toString(),
                errorKey = if (map?.containsKey("error") == true) "error" else "message"
            )
        } catch (e: Exception) {
            Error(
                errorCode = ErrorCode.UNKNOWN_ERROR,
                errorMessage = errorString, // fallback to raw error string
                errorKey = ""
            )
        }
    }

}
