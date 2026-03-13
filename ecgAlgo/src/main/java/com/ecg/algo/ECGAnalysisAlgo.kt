package com.ecg.algo

import android.util.Log
import com.neurosky.AlgoSdk.NskAlgoDataType
import com.neurosky.AlgoSdk.NskAlgoECGValueType
import com.neurosky.AlgoSdk.NskAlgoSampleRate
import com.neurosky.AlgoSdk.NskAlgoSdk
import com.neurosky.AlgoSdk.NskAlgoState
import com.neurosky.AlgoSdk.NskAlgoType

class ECGAnalysisAlgo(private val onECGAnalysisResultListener: OnECGAnalysisResultListener) {
    private val sdkLicence = "NeuroSky_Release_To_GeneralFreeLicense_Use_Only_Dec  1 2016"
    private val mNskAlgoSdk: NskAlgoSdk = NskAlgoSdk()
    private var ecgDataIndex = 0
    private val algoTypes = intArrayOf(
        NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTRATE,
        NskAlgoType.NSK_ALGO_TYPE_ECG_SMOOTH,
    )

    init {
        mNskAlgoSdk.setOnStateChangeListener { state: Int, reason: Int ->
            if (BuildConfig.DEBUG) {
                Log.e(
                    "ECGAnalysisAlgo",
                    "state:${NskAlgoState(state)}, reason: ${NskAlgoState(reason)}"
                )
            }
        }
        var algoType = 0
        for (type in algoTypes) {
            algoType = algoType or type
        }
        val ret = NskAlgoSdk.NskAlgoInit(algoType, "", sdkLicence)
        if (ret == 0) {
            if (BuildConfig.DEBUG) {
                Log.i(
                    "ECGAnalysisAlgo",
                    "ECG algo has been initialized successfully."
                )
            }
            setupSDK(algoType)
        } else if (BuildConfig.DEBUG) {
            Log.e(
                "ECGAnalysisAlgo",
                "Failed to initialize ECG algo, code = $ret"
            )
        }

    }

    private fun setupSDK(algoType: Int) {
        if (!mNskAlgoSdk.setBaudRate(
                NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG, NskAlgoSampleRate.NSK_ALGO_SAMPLE_RATE_512
            )
        ) {
            if (BuildConfig.DEBUG) {
                Log.e(
                    "ECGAnalysisAlgo",
                    "Failed to set the sampling rate: ${NskAlgoSampleRate.NSK_ALGO_SAMPLE_RATE_512}"
                )
            }
            return
        }

        var sdkVersion = "SDK ver: ${NskAlgoSdk.NskAlgoSdkVersion()}\n"
        if (algoType and NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTRATE != 0) {
            sdkVersion += "HeartRate ver: ${NskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTRATE)}\n"
        }
        if (algoType and NskAlgoType.NSK_ALGO_TYPE_ECG_SMOOTH != 0) {
            sdkVersion += "Smooth ver: ${NskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ECG_SMOOTH)}\n"
        }
        if (BuildConfig.DEBUG) {
            Log.i("ECGAnalysisAlgo", "sdkVersion $sdkVersion")
        }
        mNskAlgoSdk.setOnECGAlgoIndexListener { type: Int, value: Int ->
            when (type) {
                NskAlgoECGValueType.NSK_ALGO_ECG_VALUE_TYPE_SMOOTH -> {
                    onECGAnalysisResultListener.onOutputFilteredECGData(value)
                }
                NskAlgoECGValueType.NSK_ALGO_ECG_VALUE_TYPE_HR ->{
                    onECGAnalysisResultListener.onOutputECGResult(value)
                }
            }
        }
    }

    fun start() {
        if (BuildConfig.DEBUG) {
            Log.e("ECGAnalysisAlgo", "start()")
        }
        ecgDataIndex = 0
        NskAlgoSdk.NskAlgoStart(false)
    }

    fun stop() {
        if (BuildConfig.DEBUG) {
            Log.e("ECGAnalysisAlgo", "stop()")
        }
        NskAlgoSdk.NskAlgoPause()
        NskAlgoSdk.NskAlgoStop()
    }

    fun destroy() {
        if (BuildConfig.DEBUG) {
            Log.e("ECGAnalysisAlgo", "destroy()")
        }
        NskAlgoSdk.NskAlgoUninit()
    }

    fun inputData(data: Int) {
        if (ecgDataIndex == 0 || ecgDataIndex % 256 == 0) {
            // send the good signal for every half second
            NskAlgoSdk.NskAlgoDataStream(
                NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG_PQ,
                shortArrayOf(200.toShort()),
                1
            )
        }
        NskAlgoSdk.NskAlgoDataStream(
            NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG,
            shortArrayOf(data.toShort()),
            1
        )
        ecgDataIndex++
    }
}