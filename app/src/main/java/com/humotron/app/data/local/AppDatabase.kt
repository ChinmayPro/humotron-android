package com.humotron.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.humotron.app.data.local.converters.SleepConverters
import com.humotron.app.data.local.dao.SleepDao
import com.humotron.app.data.local.entity.HrData
import com.humotron.app.data.local.entity.HrvData
import com.humotron.app.data.local.entity.SleepEntity
import com.humotron.app.data.local.entity.StepData
import com.humotron.app.data.local.entity.TempData

@Database(
    entities = [SleepEntity::class, HrData::class, HrvData::class, StepData::class, TempData::class],
    version = 1
)
@TypeConverters(SleepConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao
}