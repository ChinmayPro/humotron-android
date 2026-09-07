package com.humotron.app.data.network.interceptor

import android.content.Context
import com.humotron.app.data.network.exceptions.NoConnectivityException
import com.humotron.app.util.NetworkUtils
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
}
