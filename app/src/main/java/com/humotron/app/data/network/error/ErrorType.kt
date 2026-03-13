package com.humotron.app.data.network.error

import com.humotron.app.R


enum class ErrorType(error: Int) {

    ERROR_INVALID_RESPONSE(R.string.unexpected_response), ERROR_REQUEST_TIME_OUT(R.string.connection_timeout), ERROR_CONNECTIVITY(R.string.no_internet), PARSING_ERROR(R.string.parsing_error);

    var errorMessage: Int = error
}
