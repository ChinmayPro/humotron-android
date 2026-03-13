package com.humotron.app.data.network.exceptions

class EmptyResponseBodyException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, throwable: Throwable?) : super(message, throwable)
    constructor() : super(null, null)
}