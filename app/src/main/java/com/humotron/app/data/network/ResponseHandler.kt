package com.humotron.app.data.network

import android.content.Context
import com.humotron.app.R
import com.humotron.app.data.network.error.Error
import com.humotron.app.data.network.error.ErrorUtils
import com.humotron.app.data.network.exceptions.EmptyResponseBodyException
import com.humotron.app.data.network.exceptions.UnauthorizedException
import com.humotron.app.data.network.exceptions.ValidationException
import org.json.JSONObject
import retrofit2.Response

class ResponseHandler(val context: Context, val errorUtils: ErrorUtils) {

    companion object {
        public const val TAG = "ResponseHandler"
    }

    fun <T : Any> handleResponse(
        response: Response<T>,
        isAutoHandleError: Boolean = true
    ): Resource<T> {

        if (response.isSuccessful) {

            val responseBody = response.body()

            return if (response.code() == 204 || response.code() == 201) {
                Resource.success(responseBody)
            } else {
                if (responseBody != null) {
                    Resource.success(responseBody)
                } else {
                    Resource.error(errorUtils.handleError(EmptyResponseBodyException(), ""))
                }

            }

        } else if (response.code() == 400) {

            val errorBody = response.errorBody()
            val error = JSONObject(errorBody?.string() ?: "").toString()


            return if (errorBody != null) {
                if (isAutoHandleError) {
                    Resource.error(
                        errorUtils.handleError(
                            ValidationException(error),
                            ""
                        )
                    )
                } else {
                    Resource.error(Error(error = error))
                }

            } else {
                Resource.error(errorUtils.handleError(null, ""))
            }

        } else if (response.code() == 401 || response.code() == 400 || response.code() == 403 || response.code() == 404 || response.code() == 423) {

            val errorBody = response.errorBody()


            val body: Error?

            if (errorBody != null) {

                val jsonObject = JSONObject(errorBody.string())

                if (jsonObject.has("detail"))

                    body = errorUtils.handleError(
                        UnauthorizedException(
                            jsonObject.getString(
                                "detail"
                            )
                        ), ""
                    )
                else if (jsonObject.has("non_field_errors")) {
                    body = errorUtils.handleError(
                        UnauthorizedException(
                            jsonObject.getString(
                                "non_field_errors"
                            )
                        ), ""
                    )
                } else {
                    body = errorUtils.handleError(null, "")
                }

            } else {
                body = errorUtils.handleError(null, "")
            }

            return if (response.code() == 401) {
                Resource.error(body)
            } else if (response.code() == 404) {
                body.errorCode = 404
                Resource.error(body)
            } else {
                Resource.error(body)
            }


        } else if (response.code() == 500) {

            return Resource.error(
                errorUtils.handleError(
                    ValidationException(context.getString(R.string.something_went_wrong)),
                    ""
                )
            )
        } else {
            return Resource.error(errorUtils.handleError(null, ""))
        }

    }

    fun <T : Any> handleException(e: Exception?): Resource<T> {
        return Resource.exception(errorUtils.handleError(e, ""))
    }
}