package com.humotron.app.core.di

import com.humotron.app.data.local.dao.SleepDao
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.remote.AppApi
import com.humotron.app.data.remote.AuthApi
import com.humotron.app.data.repository.AuthRepository
import com.humotron.app.data.repository.BioHackRepository
import com.humotron.app.data.repository.DecodeRepository
import com.humotron.app.data.repository.DeviceRepository
import com.humotron.app.data.repository.OnBoardingRepository
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        api: AuthApi,
        responseHandler: ResponseHandler,
    ): AuthRepository {
        return AuthRepository(api, responseHandler)
    }


    @Singleton
    @Provides
    fun provideSleepRepository(
        api: AppApi,
        sleepDao: SleepDao,
        prefUtils: PrefUtils,
        responseHandler: ResponseHandler,
    ): SleepRepository {
        return SleepRepository(api, sleepDao, prefUtils, responseHandler)
    }

    @Singleton
    @Provides
    fun provideDeviceRepository(
        sleepDao: SleepDao
    ): DeviceRepository {
        return DeviceRepository(sleepDao)
    }

    @Singleton
    @Provides
    fun provideOnBoardingRepository(
        api: AppApi,
        responseHandler: ResponseHandler,
        prefUtils: PrefUtils,
    ): OnBoardingRepository {
        return OnBoardingRepository(api, responseHandler,prefUtils)
    }

    @Singleton
    @Provides
    fun provideBioHackRepository(
        api: AppApi,
        responseHandler: ResponseHandler,
    ): BioHackRepository {
        return BioHackRepository(api, responseHandler)
    }

    @Singleton
    @Provides
    fun provideDecodeRepository(
        api: AppApi,
        responseHandler: ResponseHandler,
    ): DecodeRepository {
        return DecodeRepository(api, responseHandler)
    }

}