package com.humotron.app.data.network.exceptions

import java.io.IOException

class NoConnectivityException(message: String = "No Internet Connection") : IOException(message)
