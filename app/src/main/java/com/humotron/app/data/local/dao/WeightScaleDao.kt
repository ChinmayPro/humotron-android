package com.humotron.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humotron.app.data.local.entity.scale.WeightScaleMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightScaleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(measurement: WeightScaleMeasurementEntity): Long

    @Query("SELECT * FROM weight_scale_measurement WHERE sync = 0 ORDER BY measuredAt ASC")
    fun getUnSyncMeasurements(): Flow<List<WeightScaleMeasurementEntity>>

    @Query("UPDATE weight_scale_measurement SET sync = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT * FROM weight_scale_measurement ORDER BY measuredAt DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<WeightScaleMeasurementEntity?>
}
