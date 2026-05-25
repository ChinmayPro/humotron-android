package com.humotron.app.di

import android.content.Context
import com.humotron.app.util.BillingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Singleton
    @Provides
    fun provideBillingManager(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): BillingManager {
        return BillingManager(context, applicationScope)
    }
}
