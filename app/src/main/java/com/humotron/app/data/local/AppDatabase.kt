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
import com.humotron.app.data.local.entity.band.BandDetailActivityData
import com.humotron.app.data.local.entity.band.BandHrData
import com.humotron.app.data.local.entity.band.BandHrvData
import com.humotron.app.data.local.entity.band.BandSleepData
import com.humotron.app.data.local.entity.band.BandSpO2Data
import com.humotron.app.data.local.entity.band.BandTotalActivityData

@Database(
    entities = [
        SleepEntity::class,
        HrData::class,
        HrvData::class,
        StepData::class,
        TempData::class,
        BandHrvData::class,
        BandHrData::class,
        BandSpO2Data::class,
        BandDetailActivityData::class,
        BandTotalActivityData::class,
        BandSleepData::class,
    ],
    version = 4
)
@TypeConverters(SleepConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao
}