package com.humotron.app.core.di

import android.content.Context
import androidx.room.Room
import com.humotron.app.data.local.AppDatabase
import com.humotron.app.data.local.dao.SleepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "humotron_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Singleton
    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao {
        return database.sleepDao()
    }
}