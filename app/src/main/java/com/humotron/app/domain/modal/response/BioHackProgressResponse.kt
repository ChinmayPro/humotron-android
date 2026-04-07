package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BioHackProgressResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("categoryScore")
        val categoryScore: List<CategoryScore>?,
        @SerializedName("mysteryScore")
        val mysteryScore: MysteryScore?,
        @SerializedName("primaryTagScore")
        val primaryTagScore: List<PrimaryTagScore>?,
        @SerializedName("streak")
        val streak: List<Streak>?,
        @SerializedName("testPacks")
        val testPacks: List<TestPack>?
    ) : Parcelable {
        @Parcelize
        data class CategoryScore(
            @SerializedName("categoryId")
            val categoryId: String?,
            @SerializedName("categoryName")
            val categoryName: String?,
            @SerializedName("count")
            val count: Int?,
            @SerializedName("percentage")
            val percentage: Double?
        ) : Parcelable

        @Parcelize
        data class MysteryScore(
            @SerializedName("learningStatus")
            val learningStatus: String?,
            @SerializedName("levelCompletionScore")
            val levelCompletionScore: Int?,
            @SerializedName("remainingLikes")
            val remainingLikes: Int?,
            @SerializedName("totalLikes")
            val totalLikes: Int?
        ) : Parcelable

        @Parcelize
        data class PrimaryTagScore(
            @SerializedName("categoryName")
            val categoryName: String?,
            @SerializedName("count")
            val count: Int?,
            @SerializedName("percentage")
            val percentage: Int?,
            @SerializedName("primaryTagId")
            val primaryTagId: String?,
            @SerializedName("primaryTagName")
            val primaryTagName: String?,
            @SerializedName("testCount")
            val testCount: Int?,
            @SerializedName("totalTests")
            val totalTests: Int?,
            @SerializedName("unlockedScore")
            val unlockedScore: Int?
        ) : Parcelable

        @Parcelize
        data class Streak(
            @SerializedName("count")
            val count: Int?,
            @SerializedName("date")
            val date: String?,
            var day: String? = "",
            var formatDate: String? = ""
        ) : Parcelable

        @Parcelize
        data class TestPack(
            @SerializedName("difficulty")
            val difficulty: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("primaryId")
            val primaryId: PrimaryId?,
            @SerializedName("testName")
            val testName: String?,
            @SerializedName("totalQuestion")
            val totalQuestion: String?
        ) : Parcelable {
            @Parcelize
            data class PrimaryId(
                @SerializedName("_id")
                val id: String?,
                @SerializedName("tagName")
                val tagName: String?
            ) : Parcelable
        }
    }
}