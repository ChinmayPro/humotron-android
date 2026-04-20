package com.humotron.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sleepData: SleepEntity)

    @Update
    suspend fun updateSleepData(sleepData: SleepEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHrList(i: List<HrData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHrvList(i: List<HrvData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSteps(i: List<StepData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemperature(data: List<TempData>)


    @Query("select * from hr order by time desc limit 1")
    fun getLatestHr(): Flow<HrData?>

    @Query("select * from hrv order by time desc limit 1")
    fun getLatestHrv(): Flow<HrvData?>

    @Query("select * from `temp` order by time desc limit 1")
    fun getLatestTemp(): Flow<TempData?>

    @Query("select * from step order by time desc limit 1")
    fun getLatestStepData(): Flow<StepData?>

    @Query("select * from sleep_data order by endTs desc limit 1")
    fun getLatestSleepData(): Flow<SleepEntity?>


    @Query("select * from hr where sync = 0 order by time asc")
    fun getUnSyncHr(): Flow<List<HrData>>

    @Query("select * from hrv where sync = 0 order by time asc")
    fun getUnSyncHrv(): Flow<List<HrvData>>

    @Query("select * from `temp` where sync = 0 order by time asc")
    fun getUnSyncTemp(): Flow<List<TempData>>

    @Query("select * from step where sync = 0 order by time asc")
    fun getUnSyncStepData(): Flow<List<StepData>>

    @Query("select * from sleep_data where sync = 0 order by endTs asc")
    fun getUnSyncSleepData(): Flow<List<SleepEntity>>

    @Query("UPDATE hrv SET sync = 1 WHERE timeStamp IN (:timeStamps)")
    suspend fun syncHrvData(timeStamps: List<String>)

    @Query("UPDATE hr SET sync = 1 WHERE timeStamp IN (:timeStamps)")
    suspend fun syncHrData(timeStamps: List<String>)

    @Query("UPDATE sleep_data SET sync = 1 WHERE startTimeStamp IN (:timeStamps)")
    suspend fun syncSleepData(timeStamps: List<String>)

    @Query("UPDATE step SET sync = 1 WHERE timeStamp IN (:timeStamps)")
    suspend fun syncStepData(timeStamps: List<String>)

    @Query("UPDATE `temp` SET sync = 1 WHERE timeStamp IN (:timeStamps)")
    suspend fun syncTempData(timeStamps: List<String>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandHrvList(data: List<BandHrvData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandSpO2List(data: List<BandSpO2Data>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandHrList(data: List<BandHrData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandDetailActivityList(data: List<BandDetailActivityData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandTotalActivityList(data: List<BandTotalActivityData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBandSleepList(data: List<BandSleepData>)

    @Query("select * from band_hrv where sync = 0 order by measuredAt asc")
    fun getUnSyncBandHrv(): Flow<List<BandHrvData>>

    @Query("select * from band_spo2 where sync = 0 order by measuredAt asc")
    fun getUnSyncBandSpO2(): Flow<List<BandSpO2Data>>

    @Query("select * from band_hr where sync = 0 order by measuredAt asc")
    fun getUnSyncBandHr(): Flow<List<BandHrData>>

    @Query("select * from band_detail_activity where sync = 0 order by measuredAt asc")
    fun getUnSyncBandDetailActivity(): Flow<List<BandDetailActivityData>>

    @Query("select * from band_total_activity where sync = 0 order by measuredAt asc")
    fun getUnSyncBandTotalActivity(): Flow<List<BandTotalActivityData>>

    @Query("select * from band_sleep where sync = 0 order by measuredAt asc")
    fun getUnSyncBandSleep(): Flow<List<BandSleepData>>

    @Query("UPDATE band_hrv SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandHrvData(ids: List<Long>)

    @Query("UPDATE band_spo2 SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandSpO2Data(ids: List<Long>)

    @Query("UPDATE band_hr SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandHrData(ids: List<Long>)

    @Query("UPDATE band_detail_activity SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandDetailActivityData(ids: List<Long>)

    @Query("UPDATE band_total_activity SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandTotalActivityData(ids: List<Long>)

    @Query("UPDATE band_sleep SET sync = 1 WHERE id IN (:ids)")
    suspend fun syncBandSleepData(ids: List<Long>)

}