package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class ScaleUploadData(
    val hardwareId: String,
    val data: ScaleUploadDeviceData,
    val recordTimestamp: String,
)

data class ScaleUploadDeviceData(

    @SerializedName("weight")
    val weight: Double = 0.0,

    @SerializedName("waterContent")
    val waterContent: Double = 0.0,

    @SerializedName("bodyWaterRate")
    val bodyWaterRate: Double = 0.0,

    @SerializedName("proteinMass")
    val proteinMass: Double = 0.0,

    @SerializedName("protein")
    val protein: Double = 0.0,

    @SerializedName("boneMass")
    val boneMass: Double = 0.0,

    @SerializedName("mineralSaltRate")
    val mineralSaltRate: Double = 0.0,

    @SerializedName("skeletalMuscleMass")
    val skeletalMuscleMass: Double = 0.0,

    @SerializedName("muscleRate")
    val muscleRate: Double = 0.0,

    @SerializedName("bmi")
    val bmi: Double = 0.0,

    @SerializedName("visceralFat")
    val visceralFat: Double = 0.0,

    @SerializedName("obesityDegree")
    val obesityDegree: Double = 0.0,

    @SerializedName("obesityLevel")
    val obesityLevel: Double = 0.0,

    @SerializedName("muscleMass")
    val muscleMass: Double = 0.0,

    @SerializedName("muscleMassRate")
    val muscleMassRate: Double = 0.0,

    @SerializedName("leftUpperLimbMuscleWeight")
    val leftUpperLimbMuscleWeight: Double = 0.0,

    @SerializedName("rightUpperLimbMuscleWeight")
    val rightUpperLimbMuscleWeight: Double = 0.0,

    @SerializedName("lowerLeftMuscleWeight")
    val lowerLeftMuscleWeight: Double = 0.0,

    @SerializedName("lowerRightMuscleWeight")
    val lowerRightMuscleWeight: Double = 0.0,

    @SerializedName("trunkMuscleWeight")
    val trunkMuscleWeight: Double = 0.0,

    @SerializedName("leftArmMuscleRatio")
    val leftArmMuscleRatio: Double = 0.0,

    @SerializedName("rightArmMuscleRate")
    val rightArmMuscleRate: Double = 0.0,

    @SerializedName("leftLegMuscleRatio")
    val leftLegMuscleRatio: Double = 0.0,

    @SerializedName("rightLowerLimbMuscleRatio")
    val rightLowerLimbMuscleRatio: Double = 0.0,

    @SerializedName("sinewTrunkRatio")
    val sinewTrunkRatio: Double = 0.0,

    @SerializedName("bodyFatRate")
    val bodyFatRate: Double = 0.0,

    @SerializedName("leftUpperLimbFatMass")
    val leftUpperLimbFatMass: Double = 0.0,

    @SerializedName("rightUpperLimbFatMass")
    val rightUpperLimbFatMass: Double = 0.0,

    @SerializedName("leftLegFatMass")
    val leftLegFatMass: Double = 0.0,

    @SerializedName("rightLegFatMass")
    val rightLegFatMass: Double = 0.0,

    @SerializedName("trunkFatMass")
    val trunkFatMass: Double = 0.0,

    @SerializedName("leftUpperLimbFat")
    val leftUpperLimbFat: Double = 0.0,

    @SerializedName("rightUpperLimbFat")
    val rightUpperLimbFat: Double = 0.0,

    @SerializedName("leftLegFat")
    val leftLegFat: Double = 0.0,

    @SerializedName("rightLegFat")
    val rightLegFat: Double = 0.0,

    @SerializedName("trunkFat")
    val trunkFat: Double = 0.0,

    @SerializedName("fatMass")
    val fatMass: Double = 0.0,

    @SerializedName("subcutaneousFat")
    val subcutaneousFat: Double = 0.0,

    @SerializedName("subcutaneousFatMass")
    val subcutaneousFatMass: Double = 0.0,

    @SerializedName("bmr")
    val bmr: Double = 0.0,

    @SerializedName("leanBodyWeight")
    val leanBodyWeight: Double = 0.0,

    @SerializedName("metabolicAge")
    val metabolicAge: Double = 0.0,

    @SerializedName("bodyType")
    val bodyType: Double = 0.0,

    @SerializedName("weightControl")
    val weightControl: Double = 0.0,

    @SerializedName("muscleControl")
    val muscleControl: Double = 0.0,

    @SerializedName("fatControl")
    val fatControl: Double = 0.0,

    @SerializedName("standWeight")
    val standWeight: Double = 0.0,

    @SerializedName("smi")
    val smi: Double = 0.0,

    @SerializedName("waistHipRatio")
    val waistHipRatio: Double = 0.0,

    @SerializedName("healthScore")
    val healthScore: Double = 0.0,

    @SerializedName("heartRate")
    val heartRate: Double = 0.0,

    @SerializedName("heartIndex")
    val heartIndex: Double = 0.0,

    @SerializedName("fattyLiverRisk")
    val fattyLiverRisk: Double = 0.0,

    @SerializedName("bestVisualWeight")
    val bestVisualWeight: Double = 0.0,

    @SerializedName("mineralSalt")
    val mineralSalt: Double = 0.0,
)