package com.humotron.app.data.network.error

data class ErrorBean(
    val throwable: Throwable,
    val error: Int = 0,
    val errorImage: Int = 0
)
