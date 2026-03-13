package com.humotron.app.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.humotron.app.core.AppConstant.BASE_URL
import com.humotron.app.data.remote.AppApi
import com.humotron.app.data.remote.AuthApi
import com.humotron.app.util.PrefUtils
import com.pluto.plugins.network.interceptors.okhttp.PlutoOkhttpInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {


    @Provides
    @Singleton
    fun getGson(): Gson {
        return GsonBuilder().setLenient().create()
    }


    @Provides
    @Singleton
    fun getLoginInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return loggingInterceptor
    }


    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient.Builder {
        val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        okHttpBuilder.addNetworkInterceptor(loggingInterceptor)
        okHttpBuilder.addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().addHeader("Content-Type", "application/json").build()
            )
        }
        okHttpBuilder.addInterceptor(PlutoOkhttpInterceptor)

        return okHttpBuilder.connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES)

    }

    @Singleton
    @Provides
    @AppApiQualifier
    fun provideAppRetrofit(
        okHttpClientBuilder: OkHttpClient.Builder,
        gson: Gson,
        prefUtils: PrefUtils
    ): Retrofit {
        okHttpClientBuilder.addInterceptor(Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            val token = prefUtils.getAuthToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        })

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .build()
    }


    @Singleton
    @Provides
    @AuthApiQualifier
    fun provideAuthRetrofit(
        okHttpClientBuilder: OkHttpClient.Builder,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .build()
    }


    @Provides
    @Singleton
    fun provideAuthApiService(@AuthApiQualifier retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAppApiService(@AppApiQualifier retrofit: Retrofit): AppApi =
        retrofit.create(AppApi::class.java)
}