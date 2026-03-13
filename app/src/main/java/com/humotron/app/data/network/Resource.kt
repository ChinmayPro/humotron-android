package com.humotron.app.data.network
import com.humotron.app.data.network.error.Error

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val error: Error?
) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(error: Error): Resource<T> {
            return Resource(Status.ERROR, null, error)
        }

        fun <T> exception(error: Error): Resource<T> {

            return Resource(
                Status.EXCEPTION,
                null,
                error
            )
        }

        fun <T> loading(): Resource<T> {
            return Resource(
                Status.LOADING,
                null,
                null
            )
        }
    }
}