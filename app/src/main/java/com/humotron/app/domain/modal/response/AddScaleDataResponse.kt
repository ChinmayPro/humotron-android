package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddScaleDataResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?,
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("hardwareSpecificDetail")
        val hardwareSpecificDetail: HardwareSpecificDetail?,
    ) : Parcelable

    @Parcelize
    data class HardwareSpecificDetail(
        @SerializedName("userId")
        val userId: String?,
        @SerializedName("hardwareId")
        val hardwareId: String?,
        @SerializedName("data")
        val data: ScaleData?,
        @SerializedName("recordId")
        val recordId: String?,
        @SerializedName("pdfId")
        val pdfId: String?,
        @SerializedName("isDeleted")
        val isDeleted: Boolean?,
        @SerializedName("recordTimestamp")
        val recordTimestamp: Long?,
        @SerializedName("_id")
        val id: String?,
        @SerializedName("createdAt")
        val createdAt: String?,
        @SerializedName("updatedAt")
        val updatedAt: String?,
    ) : Parcelable

    @Parcelize
    data class ScaleData(
        @SerializedName("bestVisualWeight")
        val bestVisualWeight: Double?,
        @SerializedName("bmi")
        val bmi: Double?,
        @SerializedName("bmr")
        val bmr: Double?,
        @SerializedName("bodyFatRate")
        val bodyFatRate: Double?,
        @SerializedName("bodyType")
        val bodyType: Double?,
        @SerializedName("bodyWaterRate")
        val bodyWaterRate: Double?,
        @SerializedName("boneMass")
        val boneMass: Double?,
        @SerializedName("fatControl")
        val fatControl: Double?,
        @SerializedName("fatMass")
        val fatMass: Double?,
        @SerializedName("fattyLiverRisk")
        val fattyLiverRisk: Double?,
        @SerializedName("healthScore")
        val healthScore: Double?,
        @SerializedName("heartIndex")
        val heartIndex: Double?,
        @SerializedName("heartRate")
        val heartRate: Double?,
        @SerializedName("leanBodyWeight")
        val leanBodyWeight: Double?,
        @SerializedName("leftArmMuscleRatio")
        val leftArmMuscleRatio: Double?,
        @SerializedName("leftLegFat")
        val leftLegFat: Double?,
        @SerializedName("leftLegFatMass")
        val leftLegFatMass: Double?,
        @SerializedName("leftLegMuscleRatio")
        val leftLegMuscleRatio: Double?,
        @SerializedName("leftUpperLimbFat")
        val leftUpperLimbFat: Double?,
        @SerializedName("leftUpperLimbFatMass")
        val leftUpperLimbFatMass: Double?,
        @SerializedName("leftUpperLimbMuscleWeight")
        val leftUpperLimbMuscleWeight: Double?,
        @SerializedName("lowerLeftMuscleWeight")
        val lowerLeftMuscleWeight: Double?,
        @SerializedName("lowerRightMuscleWeight")
        val lowerRightMuscleWeight: Double?,
        @SerializedName("metabolicAge")
        val metabolicAge: Double?,
        @SerializedName("mineralSalt")
        val mineralSalt: Double?,
        @SerializedName("mineralSaltRate")
        val mineralSaltRate: Double?,
        @SerializedName("muscleControl")
        val muscleControl: Double?,
        @SerializedName("muscleMass")
        val muscleMass: Double?,
        @SerializedName("muscleMassRate")
        val muscleMassRate: Double?,
        @SerializedName("muscleRate")
        val muscleRate: Double?,
        @SerializedName("obesityDegree")
        val obesityDegree: Double?,
        @SerializedName("obesityLevel")
        val obesityLevel: Double?,
        @SerializedName("protein")
        val protein: Double?,
        @SerializedName("proteinMass")
        val proteinMass: Double?,
        @SerializedName("rightArmMuscleRate")
        val rightArmMuscleRate: Double?,
        @SerializedName("rightLegFat")
        val rightLegFat: Double?,
        @SerializedName("rightLegFatMass")
        val rightLegFatMass: Double?,
        @SerializedName("rightLowerLimbMuscleRatio")
        val rightLowerLimbMuscleRatio: Double?,
        @SerializedName("rightUpperLimbFat")
        val rightUpperLimbFat: Double?,
        @SerializedName("rightUpperLimbFatMass")
        val rightUpperLimbFatMass: Double?,
        @SerializedName("rightUpperLimbMuscleWeight")
        val rightUpperLimbMuscleWeight: Double?,
        @SerializedName("sinewTrunkRatio")
        val sinewTrunkRatio: Double?,
        @SerializedName("skeletalMuscleMass")
        val skeletalMuscleMass: Double?,
        @SerializedName("smi")
        val smi: Double?,
        @SerializedName("standWeight")
        val standWeight: Double?,
        @SerializedName("subcutaneousFat")
        val subcutaneousFat: Double?,
        @SerializedName("subcutaneousFatMass")
        val subcutaneousFatMass: Double?,
        @SerializedName("trunkFat")
        val trunkFat: Double?,
        @SerializedName("trunkFatMass")
        val trunkFatMass: Double?,
        @SerializedName("trunkMuscleWeight")
        val trunkMuscleWeight: Double?,
        @SerializedName("visceralFat")
        val visceralFat: Double?,
        @SerializedName("waistHipRatio")
        val waistHipRatio: Double?,
        @SerializedName("waterContent")
        val waterContent: Double?,
        @SerializedName("weight")
        val weight: Double?,
        @SerializedName("weightControl")
        val weightControl: Double?,
    ) : Parcelable
}
