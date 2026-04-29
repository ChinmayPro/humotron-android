package com.humotron.app.util

import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.data.local.entity.band.BandDetailActivityData
import com.humotron.app.data.local.entity.band.BandHrData
import com.humotron.app.data.local.entity.band.BandHrvData
import com.humotron.app.data.local.entity.band.BandSleepData
import com.humotron.app.data.local.entity.band.BandSpO2Data
import com.humotron.app.data.local.entity.band.BandTotalActivityData
import com.humotron.app.domain.repository.SleepRepository
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.callback.DataListener2025
import com.jstyle.blesdk2208a.constant.BleConst
import com.jstyle.blesdk2208a.constant.DeviceKey
import com.pluto.plugins.logger.PlutoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandSyncManager @Inject constructor(
    private val bandBleManager: BandBleManager,
    private val repository: SleepRepository,
    private val prefUtils: PrefUtils,
) {
    private enum class Stage {
        SPO2,
        DETAIL_ACTIVITY,
        STATIC_HR,
        TOTAL_ACTIVITY,
        HRV,
        SLEEP,
        DONE,
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    private var syncSessionRunning = false
    private var stage: Stage = Stage.DONE
    private var stagePacketCount = 0

    private val parseFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy.MM.dd", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    // Exposed to UI: true while pulling data from band into local DB.
    private val _isBandSyncingLocal = MutableStateFlow(false)
    val isBandSyncingLocal: StateFlow<Boolean> = _isBandSyncingLocal

    // Exposed to UI: true while sending band data to server.
    private val _isBandUploading = MutableStateFlow(false)
    val isBandUploading: StateFlow<Boolean> = _isBandUploading

    fun start() {
        if (started) return
        started = true

        scope.launch {
            bandBleManager.connectionState.collectLatest { connected ->
                if (!connected) {
                    resetSession()
                    return@collectLatest
                }

                // Kick off full pull each time a new connection session starts.
                if (!syncSessionRunning) {
                    syncSessionRunning = true
                    stage = Stage.SPO2
                    stagePacketCount = 0
                    _isBandSyncingLocal.value = true
                    PlutoLog.e(TAG_BAND_DEBUG, "new connection session start sync to local")
                    requestStageStart(stage)
                }
            }
        }

        scope.launch {
            bandBleManager.bleEvents.collectLatest { event ->
                if (event.action != BandBleManager.ACTION_DATA_AVAILABLE) return@collectLatest
                val raw = event.value ?: return@collectLatest
                if (!syncSessionRunning) return@collectLatest

                try {
                    BleSDK.DataParsingWithData(raw, object : DataListener2025 {
                        override fun dataCallback(maps: MutableMap<String, Any>?) {
                            val map = maps ?: return
                            handleParsedMap(map)
                        }

                        override fun dataCallback(value: ByteArray?) = Unit
                    })
                } catch (e: Exception) {
                    PlutoLog.e(TAG_BAND_DEBUG, "failed to parse data: ${e.toString()}")
                }
            }
        }
    }

    private fun handleParsedMap(map: Map<String, Any>) {
        PlutoLog.e(TAG_BAND_DEBUG, "handleParsedMap: $stage")
        val hardwareId = prefUtils.getBandHardwareId().orEmpty()
        if (hardwareId.isBlank()) return

        val dataType = map[DeviceKey.DataType] as? String ?: return
        //PlutoLog.e(TAG_BAND_DEBUG, "handleParsedMap DataType: $dataType")
        val end = (map[DeviceKey.End] as? Boolean) ?: false
        when (stage) {
            Stage.SPO2 -> {
                if (dataType != BleConst.GetAutomaticSpo2Monitoring) return
                insertBandSpO2(map, hardwareId)
                handleStageProgress(end)
            }

            Stage.DETAIL_ACTIVITY -> {
                if (dataType != BleConst.GetDetailActivityData) return
                insertBandDetailActivity(map, hardwareId)
                handleStageProgress(end)
            }

            Stage.STATIC_HR -> {
                if (dataType != BleConst.GetStaticHR) return
                insertBandStaticHr(map, hardwareId)
                handleStageProgress(end)
            }

            Stage.TOTAL_ACTIVITY -> {
                if (dataType != BleConst.GetTotalActivityData) return
                insertBandTotalActivity(map, hardwareId)
                handleStageProgress(end)
            }

            Stage.HRV -> {
                if (dataType != BleConst.GetHRVData) return
                insertBandHrv(map, hardwareId)
                handleStageProgress(end)
            }

            Stage.SLEEP -> {
                if (dataType != BleConst.GetDetailSleepData) return
                insertBandSleep(map, hardwareId)
                handleStageProgress(end)
            }


            Stage.DONE -> Unit
        }
    }

    private fun handleStageProgress(end: Boolean) {
        stagePacketCount += 1
        if (!end) {
            if (stagePacketCount % 50 == 0) {
                requestStageContinue(stage)
            }
            return
        }

        val next = nextStage(stage)
        if (next == Stage.DONE) {
            // Local sync phase is done once all stages are saved into Room.
            PlutoLog.e(TAG_BAND_DEBUG, "local sync done")
            _isBandSyncingLocal.value = false
            scope.launch {
                try {
                    _isBandUploading.value = true
                    repository.syncBandDataOnce()
                } catch (_: Exception) {
                } finally {
                    // API call finished (success or failure): uploading ends.
                    _isBandUploading.value = false
                    resetSession()
                }
            }
            return
        }
        stage = next
        stagePacketCount = 0
        requestStageStart(stage)
    }

    private fun requestStageStart(stage: Stage) {
        PlutoLog.e(TAG_BAND_DEBUG, "requestStageStart: $stage")
        when (stage) {
            Stage.SPO2 ->
                bandBleManager.writeValue(
                    BleSDK.Obtain_The_data_of_manual_blood_oxygen_test(0x00.toByte(), ""),
                )

            Stage.DETAIL_ACTIVITY ->
                bandBleManager.writeValue(BleSDK.GetDetailActivityDataWithMode(0x00.toByte(), ""))

            Stage.STATIC_HR ->
                bandBleManager.writeValue(BleSDK.GetStaticHRWithMode(0x00.toByte(), ""))

            Stage.TOTAL_ACTIVITY ->
                bandBleManager.writeValue(BleSDK.GetTotalActivityDataWithMode(0x00.toByte(), ""))

            Stage.HRV ->
                bandBleManager.writeValue(BleSDK.GetHRVDataWithMode(0x00.toByte(), ""))

            Stage.SLEEP ->
                bandBleManager.writeValue(BleSDK.GetDetailSleepDataWithMode(0x00.toByte(), ""))

            Stage.DONE -> Unit
        }
    }

    private fun requestStageContinue(stage: Stage) {
        when (stage) {
            Stage.SPO2 ->
                bandBleManager.writeValue(
                    BleSDK.Obtain_The_data_of_manual_blood_oxygen_test(0x02.toByte(), ""),
                )

            Stage.DETAIL_ACTIVITY ->
                bandBleManager.writeValue(BleSDK.GetDetailActivityDataWithMode(0x02.toByte(), ""))

            Stage.STATIC_HR ->
                bandBleManager.writeValue(BleSDK.GetStaticHRWithMode(0x02.toByte(), ""))

            Stage.TOTAL_ACTIVITY ->
                bandBleManager.writeValue(BleSDK.GetTotalActivityDataWithMode(0x02.toByte(), ""))

            Stage.HRV ->
                bandBleManager.writeValue(BleSDK.GetHRVDataWithMode(0x02.toByte(), ""))

            Stage.SLEEP ->
                bandBleManager.writeValue(BleSDK.GetDetailSleepDataWithMode(0x02.toByte(), ""))

            Stage.DONE -> Unit
        }
    }

    private fun nextStage(current: Stage): Stage =
        when (current) {
            Stage.SPO2 -> Stage.DETAIL_ACTIVITY
            Stage.DETAIL_ACTIVITY -> Stage.STATIC_HR
            Stage.STATIC_HR -> Stage.TOTAL_ACTIVITY
            Stage.TOTAL_ACTIVITY -> Stage.HRV
            Stage.HRV -> Stage.SLEEP
            Stage.SLEEP -> Stage.DONE
            Stage.DONE -> Stage.DONE
        }

    private fun insertBandSpO2(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, String>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = normalizeDate(row[DeviceKey.Date] ?: return@mapNotNull null)
            val measuredAt = parseEpochMillis(date) ?: return@mapNotNull null
            BandSpO2Data(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                automaticSpo2Data = row[DeviceKey.Blood_oxygen]?.toIntOrNull() ?: 0,
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandSpO2List(entities) }
        }
    }

    private fun insertBandDetailActivity(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, String>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = normalizeDate(row[DeviceKey.Date] ?: return@mapNotNull null)
            val measuredAt = parseEpochMillis(date) ?: return@mapNotNull null
            val arraySteps =
                row[DeviceKey.ArraySteps].orEmpty().trim()
                    .split(" ")
                    .mapNotNull { it.toIntOrNull() }

            BandDetailActivityData(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                arraySteps = arraySteps,
                step = row[DeviceKey.KDetailMinterStep]?.toIntOrNull() ?: 0,
                distance = row[DeviceKey.Distance]?.toDoubleOrNull() ?: 0.0,
                calories = row[DeviceKey.Calories]?.toDoubleOrNull() ?: 0.0,
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandDetailActivityList(entities) }
        }
    }

    private fun insertBandStaticHr(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, String>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = normalizeDate(row[DeviceKey.Date] ?: return@mapNotNull null)
            val measuredAt = parseEpochMillis(date) ?: return@mapNotNull null
            BandHrData(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                singleHR = row[DeviceKey.StaticHR]?.toIntOrNull() ?: 0,
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandHrList(entities) }
        }
    }

    private fun insertBandTotalActivity(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, String>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = normalizeDate(row[DeviceKey.Date] ?: return@mapNotNull null)
            val measuredAt = parseEpochMillis(date) ?: return@mapNotNull null
            BandTotalActivityData(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                goal = row[DeviceKey.Goal].orEmpty(),
                distance = row[DeviceKey.Distance]?.toDoubleOrNull() ?: 0.0,
                calories = row[DeviceKey.Calories]?.toDoubleOrNull() ?: 0.0,
                activeMinutes = row[DeviceKey.ActiveMinutes]?.toIntOrNull() ?: 0,
                step = row[DeviceKey.Step]?.toIntOrNull() ?: 0,
                exerciseMinutes = row[DeviceKey.ExerciseMinutes]?.toIntOrNull() ?: 0,
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandTotalActivityList(entities) }
        }
    }

    private fun insertBandHrv(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, String>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = normalizeDate(row[DeviceKey.Date] ?: return@mapNotNull null)
            val measuredAt = parseEpochMillis(date) ?: return@mapNotNull null
            BandHrvData(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                highBP = row[DeviceKey.highBP]?.toIntOrNull() ?: 0,
                lowBP = row[DeviceKey.lowBP]?.toIntOrNull() ?: 0,
                heartRate = row[DeviceKey.HeartRate]?.toIntOrNull() ?: 0,
                stress = row[DeviceKey.Stress]?.toIntOrNull() ?: 0,
                hrv = row[DeviceKey.HRV]?.toIntOrNull() ?: 0,
                vascularAging = row[DeviceKey.VascularAging]?.toIntOrNull() ?: 0,
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandHrvList(entities) }
        }
    }

    private fun insertBandSleep(map: Map<String, Any>, hardwareId: String) {
        @Suppress("UNCHECKED_CAST")
        val rows = map[DeviceKey.Data] as? List<Map<String, Any>> ?: return
        val entities = rows.mapNotNull { row ->
            val date = row[DeviceKey.Date] as? String ?: return@mapNotNull null
            val measuredAt = parseEpochMillis(normalizeDate(date)) ?: return@mapNotNull null
            val arraySleepQuality = (row[DeviceKey.ArraySleep] as? String)
                .orEmpty()
                .trim()
                .split(" ")
                .mapNotNull { it.toIntOrNull() }
            val sleepUnitLength = (row[DeviceKey.sleepUnitLength] as? String)?.toIntOrNull()
                ?: (row[DeviceKey.sleepUnitLength] as? Int)
                ?: 0

            BandSleepData(
                hardwareId = hardwareId,
                measuredAt = measuredAt,
                date = date,
                arraySleepQuality = arraySleepQuality,
                totalSleepTime = arraySleepQuality.size * sleepUnitLength,
                sleepUnitLength = sleepUnitLength,
                startTimeSleepData = (row["startTime_SleepData"] as? String).orEmpty(),
            )
        }
        if (entities.isNotEmpty()) {
            scope.launch { repository.insertBandSleepList(entities) }
        }
    }

    private fun normalizeDate(raw: String): String = raw.replace('.', '-')

    private fun parseEpochMillis(value: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd",
            "yyyy-MM-dd"
        )

        for (pattern in formats) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                val date = format.parse(value) ?: continue
                return date.time
            } catch (e: Exception) {
                //PlutoLog.e(TAG_BAND_DEBUG, "failed to parse date: $value ${e.toString()}")
            }
        }
        return null
    }

    private fun resetSession() {
        syncSessionRunning = false
        stage = Stage.DONE
        stagePacketCount = 0
        _isBandSyncingLocal.value = false
        _isBandUploading.value = false
    }
}

