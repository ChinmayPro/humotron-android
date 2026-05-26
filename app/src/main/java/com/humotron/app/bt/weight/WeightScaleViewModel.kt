package com.humotron.app.bt.weight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.local.entity.scale.WeightScaleMeasurementEntity
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.response.AddDeviceDataResponse
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.modal.response.AddScaleDataResponse
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightScaleViewModel @Inject constructor(
    private val sdkManager: WeightScaleSdkManager,
    private val repository: SleepRepository,
    private val prefUtils: PrefUtils,
) : ViewModel() {

    private val addHardwareLiveData: MutableLiveData<Resource<AddHardwareResponse>> =
        MutableLiveData()

    private val _saveMeasurementStatus = MutableStateFlow<Resource<AddScaleDataResponse>?>(null)
    val saveMeasurementStatus = _saveMeasurementStatus.asStateFlow()

    fun addHardwareData(): LiveData<Resource<AddHardwareResponse>> {
        return addHardwareLiveData
    }

    fun addHardwareInProfile(param: AddHardware) {
        repository.addHardwareInProfile(param).onEach { state ->
            addHardwareLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveAndUploadMeasurement(measurement: WeightScaleMeasurement) {
        viewModelScope.launch {
            _saveMeasurementStatus.value = Resource.loading()
            val entity = measurement.toEntity()
            repository.insertWeightScaleMeasurement(entity)
            val result = repository.syncWeightScaleDataOnce()
            _saveMeasurementStatus.value = result
        }
    }

    private fun WeightScaleMeasurement.toEntity(): WeightScaleMeasurementEntity {
        return WeightScaleMeasurementEntity(
            hardwareId = prefUtils.getWeightHardwareId() ?: "",
            measuredAt = measuredAt?.time ?: 0,
            weight = weightKg,
            waterContent = findMetricValue("water content"),
            bodyWaterRate = findMetricValue("body water rate"),
            proteinMass = findMetricValue("protein mass"),
            protein = findMetricValue("protein"),
            boneMass = findMetricValue("bone mass"),
            skeletalMuscleMass = findMetricValue("skeletal muscle mass"),
            muscleRate = findMetricValue("muscle rate"),
            bmi = findMetricValue("BMI"),
            visceralFat = findMetricValue("visceral fat"),
            obesityDegree = findMetricValue("obesity degree"),
            obesityLevel = findMetricValue("obesity level"),
            muscleMass = findMetricValue("muscle mass"),
            leftUpperLimbMuscleWeight = findMetricValue("left upper limb muscle weight"),
            rightUpperLimbMuscleWeight = findMetricValue("right upper limb muscle weight"),
            lowerLeftMuscleWeight = findMetricValue("lower left muscle weight"),
            lowerRightMuscleWeight = findMetricValue("lower right muscle weight"),
            trunkMuscleWeight = findMetricValue("trunk muscle weight"),
            leftArmMuscleRatio = findMetricValue("left arm muscle ratio"),
            rightArmMuscleRate = findMetricValue("right arm muscle rate"),
            leftLegMuscleRatio = findMetricValue("left leg muscle ratio"),
            rightLowerLimbMuscleRatio = findMetricValue("right lower limb muscle ratio"),
            sinewTrunkRatio = findMetricValue("sinew trunk ratio"),
            bodyFatRate = findMetricValue("body fat rate"),
            leftUpperLimbFatMass = findMetricValue("left upper limb fat mass"),
            rightUpperLimbFatMass = findMetricValue("right upper limb fat mass"),
            leftLegFatMass = findMetricValue("left leg fat mass"),
            rightLegFatMass = findMetricValue("right leg fat mass"),
            trunkFatMass = findMetricValue("trunk fat mass"),
            leftUpperLimbFat = findMetricValue("left upper limb fat"),
            rightUpperLimbFat = findMetricValue("right upper limb fat"),
            leftLegFat = findMetricValue("left leg fat"),
            rightLegFat = findMetricValue("right leg fat"),
            trunkFat = findMetricValue("trunk fat"),
            fatMass = findMetricValue("fat mass"),
            subcutaneousFat = findMetricValue("subcutaneous fat"),
            subcutaneousFatMass = findMetricValue("subcutaneous fat mass"),
            bmr = findMetricValue("BMR"),
            leanBodyWeight = findMetricValue("lean body weight"),
            metabolicAge = findMetricValue("metabolic age"),
            bodyType = findMetricValue("body type"),
            weightControl = findMetricValue("weight control"),
            muscleControl = findMetricValue("muscle control"),
            fatControl = findMetricValue("fat control"),
            standWeight = findMetricValue("standard weight"),
            smi = findMetricValue("smi"),
            waistHipRatio = findMetricValue("waist hip ratio"),
            healthScore = findMetricValue("health score"),
            heartRate = findMetricValue("heart rate"),
            heartIndex = findMetricValue("heart index"),
            fattyLiverRisk = findMetricValue("fatty liver risk"),
            mineralSaltRate = findMetricValue("mineral salt rate"),
            bestVisualWeight = findMetricValue("best visual weight"),
            muscleMassRate = findMetricValue("muscle mass rate"),
            mineralSalt = findMetricValue("mineral salt")
        )
    }

    private fun WeightScaleMeasurement.findMetricValue(metricName: String): Double {
        return bodyMetrics.firstOrNull {
            it.name?.contains(metricName, ignoreCase = true) == true
        }?.value ?: 0.0
    }

    val sdkState: LiveData<WeightScaleSdkState> = sdkManager.sdkState
    val bluetoothState: LiveData<String> = sdkManager.bluetoothState
    val scanState: LiveData<WeightScaleScanState> = sdkManager.scanState
    val devices: LiveData<List<WeightScaleDeviceSummary>> = sdkManager.devices
    val connectionState: LiveData<WeightScaleConnectionState> = sdkManager.connectionState
    val measurementState: LiveData<WeightScaleMeasurementState> = sdkManager.measurementState
    val latestMeasurement: LiveData<WeightScaleMeasurement?> = sdkManager.latestMeasurement
    val events: LiveData<WeightScaleEvent> = sdkManager.events
    val errors: LiveData<WeightScaleError> = sdkManager.errors

    fun initializeSdk() = sdkManager.initializeSdk()

    fun startScan() = sdkManager.startScan()

    fun stopScan() = sdkManager.stopScan()

    fun connect(deviceMac: String, userProfile: WeightScaleUserProfile) {
        sdkManager.connect(deviceMac, userProfile)
    }

    fun disconnect() = sdkManager.disconnect()

    fun reconnect() = sdkManager.reconnect()

    fun clearMeasurements() = sdkManager.clearMeasurements()
}
