package com.humotron.app.data.network.error

data class Error(
    var errorCode: Int = 0,
    val errorMessage: String? = "",
    val errorKey: String? = "",
    val errorTag: String? = null,
    val error: String? = null
)
