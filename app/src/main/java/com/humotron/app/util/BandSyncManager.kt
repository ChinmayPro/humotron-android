package com.humotron.app.util

import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.data.local.entity.BandHrvData
import com.humotron.app.domain.repository.SleepRepository
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.callback.DataListener2025
import com.jstyle.blesdk2208a.constant.BleConst
import com.jstyle.blesdk2208a.constant.DeviceKey
import com.pluto.Pluto
import com.pluto.plugins.logger.PlutoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    private var dataPacketCount = 0
    private var syncSessionRunning = false

    private val parseFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US),
    )

    fun start() {
        if (started) return
        started = true

        scope.launch {
            bandBleManager.connectionState.collectLatest { connected ->
                if (!connected) {
                    PlutoLog.e("BandSyncManager", "Device Not connected.")
                    syncSessionRunning = false
                    dataPacketCount = 0
                    return@collectLatest
                }

                // If a band is already connected on app start, begin sync immediately.
                if (!syncSessionRunning) {
                    PlutoLog.e("BandSyncManager", "syncSessionRunning starting now.")
                    syncSessionRunning = true
                    dataPacketCount = 0
                    requestHrvStart()
                }
            }
        }

        scope.launch {
            bandBleManager.bleEvents.collectLatest { event ->
                if (event.action != BandBleManager.ACTION_DATA_AVAILABLE) return@collectLatest
                val raw = event.value ?: return@collectLatest

                try {
                    BleSDK.DataParsingWithData(raw, object : DataListener2025 {
                        override fun dataCallback(maps: MutableMap<String, Any>?) {
                            val map = maps ?: return
                            val dataType = map[DeviceKey.DataType] as? String ?: return
                            if (dataType != BleConst.GetHRVData) return
                            handleHrvPayload(map)
                        }

                        override fun dataCallback(value: ByteArray?) = Unit
                    })
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun handleHrvPayload(map: Map<String, Any>) {
        val hardwareId = prefUtils.getBandHardwareId().orEmpty()
        if (hardwareId.isBlank()) return

        @Suppress("UNCHECKED_CAST")
        val data = map[DeviceKey.Data] as? List<Map<String, String>> ?: emptyList()
        val entities = data.mapNotNull { item -> item.toBandEntity(hardwareId) }

        scope.launch {
            if (entities.isNotEmpty()) {
                PlutoLog.e("BandSyncManager", "Inserting ${entities.size} entities.")
                repository.insertBandHrvList(entities)
            }
        }

        dataPacketCount += 1
        val end = (map[DeviceKey.End] as? Boolean) ?: false
        if (end) {
            syncSessionRunning = false
            scope.launch {
                PlutoLog.e("BandSyncManager", "syncSessionRunning ending now.")
                repository.syncBandDataOnce()
            }
            return
        }

        if (dataPacketCount % 50 == 0) {
            requestHrvContinue()
        }
    }

    private fun requestHrvStart() {
        // 0x00 = start, same as SDK demo HrvDataReadActivity
        bandBleManager.writeValue(BleSDK.GetHRVDataWithMode(0x00.toByte(), ""))
    }

    private fun requestHrvContinue() {
        // 0x02 = continue, same as SDK demo HrvDataReadActivity
        bandBleManager.writeValue(BleSDK.GetHRVDataWithMode(0x02.toByte(), ""))
    }

    private fun Map<String, String>.toBandEntity(hardwareId: String): BandHrvData? {
        val rawDate = this[DeviceKey.Date] ?: return null
        val normalizedDate = rawDate.replace('.', '-')
        val measuredAt =
            parseEpochMillis(rawDate) ?: parseEpochMillis(normalizedDate) ?: return null

        return BandHrvData(
            hardwareId = hardwareId,
            measuredAt = measuredAt,
            date = normalizedDate,
            highBP = this[DeviceKey.highBP]?.toIntOrNull() ?: 0,
            lowBP = this[DeviceKey.lowBP]?.toIntOrNull() ?: 0,
            heartRate = this[DeviceKey.HeartRate]?.toIntOrNull() ?: 0,
            stress = this[DeviceKey.Stress]?.toIntOrNull() ?: 0,
            hrv = this[DeviceKey.HRV]?.toIntOrNull() ?: 0,
            vascularAging = this[DeviceKey.VascularAging]?.toIntOrNull() ?: 0,
        )
    }

    private fun parseEpochMillis(value: String): Long? {
        for (format in parseFormats) {
            try {
                val date = format.parse(value) ?: continue
                return date.time
            } catch (_: Exception) {
            }
        }
        return null
    }
}

