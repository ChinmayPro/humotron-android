package com.humotron.app.ui.device

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.databinding.ActivityBandTestBinding
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.callback.DataListener2025
import com.jstyle.blesdk2208a.constant.BleConst
import com.jstyle.blesdk2208a.constant.DeviceKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Queue
import javax.inject.Inject

@AndroidEntryPoint
class BandTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBandTestBinding

    @Inject
    lateinit var bandBleManager: BandBleManager

    private val commandQueue: Queue<ByteArray> = ArrayDeque()
    private var isProcessing = false

    private val packetCounts = mutableMapOf<String, Int>()
    private val MODE_START: Byte = 0x00
    private val MODE_CONTINUE: Byte = 0x02

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBandTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogs.movementMethod = ScrollingMovementMethod()

        binding.btnGetBatteryAndMac.setOnClickListener {
            sendBatteryAndMac()
        }

        binding.btnSyncAll.setOnClickListener {
            syncAllCommands()
        }

        observeBleEvents()
    }

    private fun sendBatteryAndMac() {
        log("--- Sending Battery and MAC commands ---")
        enqueueCommand(BleSDK.GetDeviceBatteryLevel())
        enqueueCommand(BleSDK.GetDeviceMacAddress())
    }

    private fun syncAllCommands() {
        log("--- Starting Full Sync with Paging (50) ---")
        packetCounts.clear()
        
        enqueueCommand(BleSDK.GetDeviceBatteryLevel())
        enqueueCommand(BleSDK.GetDeviceMacAddress())
        enqueueCommand(BleSDK.Obtain_The_data_of_manual_blood_oxygen_test(MODE_START, ""))
        enqueueCommand(BleSDK.GetDetailActivityDataWithMode(MODE_START, ""))
        enqueueCommand(BleSDK.GetStaticHRWithMode(MODE_START, ""))
        enqueueCommand(BleSDK.GetTotalActivityDataWithMode(MODE_START, ""))
        enqueueCommand(BleSDK.GetHRVDataWithMode(MODE_START, ""))
    }

    private fun enqueueCommand(cmd: ByteArray) {
        commandQueue.offer(cmd)
        processNext()
    }

    private fun processNext() {
        if (isProcessing) return
        val cmd = commandQueue.poll() ?: return

        isProcessing = true
        bandBleManager.writeValue(cmd)
    }

    private fun observeBleEvents() {
        lifecycleScope.launch {
            bandBleManager.bleEvents.collect { event ->
                if (event.action != BandBleManager.ACTION_DATA_AVAILABLE) return@collect
                val value = event.value ?: return@collect

                BleSDK.DataParsingWithData(value, object : DataListener2025 {

                    override fun dataCallback(maps: MutableMap<String, Any>?) {
                        if (maps == null) return
                        val dataType = maps[DeviceKey.DataType] as? String ?: return

                        runOnUiThread {
                            val end = maps[DeviceKey.End] as? Boolean ?: false
                            val currentCount = (packetCounts[dataType] ?: 0) + 1
                            packetCounts[dataType] = currentCount

                            when (dataType) {
                                BleConst.GetDeviceBatteryLevel -> {
                                    val data = maps[DeviceKey.Data] as? Map<*, *>
                                    val level = data?.get(DeviceKey.BatteryLevel) as? String
                                    binding.tvBattery.text = "Battery: $level%"
                                    log("  -> Battery: $level%")
                                }

                                BleConst.GetDeviceMacAddress -> {
                                    val data = maps[DeviceKey.Data] as? Map<*, *>
                                    val mac = data?.get(DeviceKey.MacAddress) as? String
                                    binding.tvMac.text = "MAC Address: $mac"
                                    log("  -> MAC: $mac")
                                }

                                BleConst.GetAutomaticSpo2Monitoring -> {
                                    log("  -> SPO2 Data received")
                                    handlePaging(dataType, currentCount, end) {
                                        BleSDK.Obtain_The_data_of_manual_blood_oxygen_test(MODE_CONTINUE, "")
                                    }
                                }

                                BleConst.GetDetailActivityData -> {
                                    log("  -> GetDetailActivityData Data received")
                                    handlePaging(dataType, currentCount, end) {
                                        BleSDK.GetDetailActivityDataWithMode(MODE_CONTINUE, "")
                                    }
                                }

                                BleConst.GetStaticHR -> {
                                    log("  -> GetStaticHR Data received")
                                    handlePaging(dataType, currentCount, end) {
                                        BleSDK.GetStaticHRWithMode(MODE_CONTINUE, "")
                                    }
                                }

                                BleConst.GetTotalActivityData -> {
                                    log("  -> GetTotalActivityData Data received")
                                    handlePaging(dataType, currentCount, end) {
                                        BleSDK.GetTotalActivityDataWithMode(MODE_CONTINUE, "")
                                    }
                                }

                                BleConst.GetHRVData -> {
                                    log("  -> GetHRVData Data received")
                                    handlePaging(dataType, currentCount, end) {
                                        BleSDK.GetHRVDataWithMode(MODE_CONTINUE, "")
                                    }
                                }
                            }

                            if (end) {
                                log("✅ Completed $dataType (Total Packets: $currentCount)")
                                packetCounts[dataType] = 0 // Reset for next sync
                                isProcessing = false
                                processNext()
                            }
                        }
                    }

                    override fun dataCallback(value: ByteArray?) {}
                })
            }
        }
    }

    private fun handlePaging(dataType: String, count: Int, end: Boolean, continueCmd: () -> ByteArray) {
        if (!end && count % 50 == 0) {
            log("➡️ $dataType reached 50 packets, sending CONTINUE...")
            bandBleManager.writeValue(continueCmd())
        }
    }

    private fun log(message: String) {
        binding.tvLogs.append("$message\n")
        val scrollAmount =
            binding.tvLogs.layout?.let { it.getLineTop(it.lineCount) - binding.tvLogs.height } ?: 0
        if (scrollAmount > 0) {
            binding.tvLogs.scrollTo(0, scrollAmount)
        }
    }
}
