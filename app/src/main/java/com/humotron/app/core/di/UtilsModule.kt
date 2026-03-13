package com.humotron.app.core.di

import android.content.Context
import android.content.SharedPreferences
import com.humotron.app.core.Preference
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.error.ErrorUtils
import com.humotron.app.util.PrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilsModule {

    @Singleton
    @Provides
    fun provideErrorUtils(@ApplicationContext context: Context): ErrorUtils {
        return ErrorUtils(context)
    }

    @Singleton
    @Provides
    fun provideResponseHandler(
        @ApplicationContext context: Context,
        errorUtils: ErrorUtils,
    ): ResponseHandler {
        return ResponseHandler(context, errorUtils)
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Preference.PREF_NAME, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun providePrefUtils(preferences: SharedPreferences): PrefUtils {
        return PrefUtils(preferences)
    }
}