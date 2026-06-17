package com.humotron.app.data.local.dao.bpmachine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humotron.app.data.local.entity.bpmachine.BpData
import com.humotron.app.data.local.entity.bpmachine.EcgData
import kotlinx.coroutines.flow.Flow

@Dao
interface BpMachineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBpData(data: BpData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEcgData(data: EcgData)

    @Query("SELECT fileName FROM bp_data")
    suspend fun getAllBpFileNames(): List<String>

    @Query("SELECT fileName FROM ecg_data")
    suspend fun getAllEcgFileNames(): List<String>

    @Query("SELECT * FROM bp_data WHERE sync = 0 LIMIT 1")
    suspend fun getUnsyncedBpData(): BpData?

    @Query("UPDATE bp_data SET sync = :synced WHERE id = :id")
    suspend fun updateBpSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM ecg_data WHERE sync = 0 LIMIT 1")
    suspend fun getUnsyncedEcgData(): EcgData?

    @Query("UPDATE ecg_data SET sync = :synced WHERE id = :id")
    suspend fun updateEcgSyncStatus(id: Long, synced: Boolean)
}
