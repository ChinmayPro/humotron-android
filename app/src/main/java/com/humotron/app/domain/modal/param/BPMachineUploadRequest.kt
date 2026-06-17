package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class BPMachineUploadRequest(
    val hardwareId: String,
    val data: BPMachineData,
    val recordTimestamp: String,
)

data class BPMachineData(
    @SerializedName("time")
    val time: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("measureTime")
    val measureTime: Long? = null,

    @SerializedName("systolicPressure")
    val systolicPressure: Int? = null,

    @SerializedName("diastolicPressure")
    val diastolicPressure: Int? = null,

    @SerializedName("meanPressure")
    val meanPressure: Int? = null,

    @SerializedName("pulseRate")
    val pulseRate: Int? = null,

    @SerializedName("arrhythmia")
    val arrhythmia: Boolean? = null,

    @SerializedName("fileName")
    val fileName: String? = null,

    @SerializedName("recordingTime")
    val recordingTime: Int? = null,

    @SerializedName("result")
    val result: Int? = null,

    @SerializedName("hr")
    val hr: Int? = null,

    @SerializedName("qrs")
    val qrs: Int? = null,

    @SerializedName("pvcs")
    val pvcs: Int? = null,

    @SerializedName("qtc")
    val qtc: Int? = null,

    @SerializedName("connectCable")
    val connectCable: Boolean? = null,

    @SerializedName("diagnosis")
    val diagnosis: BPMachineDiagnosisData? = null,
)

data class BPMachineDiagnosisData(
    @SerializedName("isRegular")
    val isRegular: Boolean? = null,

    @SerializedName("isPoorSignal")
    val isPoorSignal: Boolean? = null,

    @SerializedName("isLeadOff")
    val isLeadOff: Boolean? = null,

    @SerializedName("isFastHr")
    val isFastHr: Boolean? = null,

    @SerializedName("isSlowHr")
    val isSlowHr: Boolean? = null,

    @SerializedName("isIrregular")
    val isIrregular: Boolean? = null,

    @SerializedName("isPvcs")
    val isPvcs: Boolean? = null,

    @SerializedName("isHeartPause")
    val isHeartPause: Boolean? = null,

    @SerializedName("isFibrillation")
    val isFibrillation: Boolean? = null,

    @SerializedName("isWideQrs")
    val isWideQrs: Boolean? = null,

    @SerializedName("isProlongedQtc")
    val isProlongedQtc: Boolean? = null,

    @SerializedName("isShortQtc")
    val isShortQtc: Boolean? = null,

    @SerializedName("resultMess")
    val resultMess: String? = null,
)
