package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class IdealPostcodesResponse(
    @SerializedName("result")
    val result: List<IdealPostcodeResult>?,
    @SerializedName("code")
    val code: Int?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("page")
    val page: Int?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("limit")
    val limit: Int?
)

data class IdealPostcodeResult(
    @SerializedName("id")
    val id: String?,
    @SerializedName("postal_county")
    val postalCounty: String?,
    @SerializedName("post_town")
    val postTown: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("dataset")
    val dataset: String?,
    @SerializedName("building_number")
    val buildingNumber: String?,
    @SerializedName("po_box")
    val poBox: String?,
    @SerializedName("dependant_locality")
    val dependantLocality: String?,
    @SerializedName("premise")
    val premise: String?,
    @SerializedName("line_1")
    val line1: String?,
    @SerializedName("line_2")
    val line2: String?,
    @SerializedName("line_3")
    val line3: String?,
    @SerializedName("district")
    val district: String?,
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("thoroughfare")
    val thoroughfare: String?,
    @SerializedName("building_name")
    val buildingName: String?,
    @SerializedName("sub_building_name")
    val subBuildingName: String?,
    @SerializedName("county")
    val county: String?
)
