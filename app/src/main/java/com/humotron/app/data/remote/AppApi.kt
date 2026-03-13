package com.humotron.app.data.remote

import com.humotron.app.data.local.entity.UploadData
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.CompleteOnboardingParam
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.DailyCalculatedMetricsParam
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.SubmitPersonalInfoParam
import com.humotron.app.domain.modal.param.WeightHeightParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.AddDeviceDataResponse
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.modal.response.AddToCartResponse
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.BioHackProgressResponse
import com.humotron.app.domain.modal.response.BookDetailResponse
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.DailyCalculatedMetricsResponse
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.HardwareListData
import com.humotron.app.domain.modal.response.MetricResponse
import com.humotron.app.domain.modal.response.NuggetDetailResponse
import com.humotron.app.domain.modal.response.NuggetPreference
import com.humotron.app.domain.modal.response.NuggetsReactionResponse
import com.humotron.app.domain.modal.response.NuggetsTypeAndLevelResponse
import com.humotron.app.domain.modal.response.RingReadingData
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.UseCaseResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import com.humotron.app.domain.modal.response.WristBandSleepDurationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AppApi {


    // user detail
    @POST("user/updateUserDetailByUserId/{userId}")
    suspend fun submitPersonalInfo(
        @Path("userId") userId: String,
        @Body data: SubmitPersonalInfoParam,
    ): Response<VerifyOtpResponse>

    @POST("user/updateUserDetailByUserId/{userId}")
    suspend fun submitWeightHeight(
        @Path("userId") userId: String,
        @Body data: WeightHeightParam,
    ): Response<VerifyOtpResponse>

    @POST("user/updateUserDetailByUserId/{userId}")
    suspend fun completeOnboarding(
        @Path("userId") userId: String,
        @Body data: CompleteOnboardingParam,
    ): Response<VerifyOtpResponse>

    @POST("interests/getAllInterest")
    suspend fun getInterests(): Response<UseCaseResponse>


    // device

    @POST("hardwareSpecificDetail/addHardwareSpecificDetail")
    suspend fun sendDataToServer(@Body data: UploadData): Response<AddDeviceDataResponse>

    @POST("userHardware/addUserHardware")
    suspend fun addHardwareId(@Body data: AddHardware): Response<AddHardwareResponse>

    @POST("device/getAllDeviceByUserId")
    suspend fun getAllDeviceData(): Response<GetAllDeviceResponse>

    @GET("userHardware/getHardwareListByUserId")
    suspend fun getHardwareList(): Response<HardwareListData>

    @POST("device/getRingReadingData/{deviceId}")
    suspend fun getRingReadingData(
        @Path("deviceId") deviceId: String,
    ): Response<RingReadingData>

    @POST("device/{endpoint}/{ringId}")
    suspend fun getRingReadingGraphData(
        @Path("endpoint") endpoint: String,
        @Path("ringId") ringId: String,
        @Body param: RingReadingParam,
    ): Response<TemperatureResponse>

    @POST("device/wristBandApi/{device_id}")
    suspend fun getWristBandGraphData(
        @Path("device_id") deviceId: String,
        @Body param: WristBandApiParam,
    ): Response<TemperatureResponse>

    @POST("device/getDailyCalculatedMetrics/{device_id}")
    suspend fun getDailyCalculatedMetrics(
        @Path("device_id") deviceId: String,
        @Body param: DailyCalculatedMetricsParam
    ): Response<DailyCalculatedMetricsResponse>

    @POST("metric/getRecommendationsByMetricId/{metricId}")
    suspend fun getRecommendationsByMetricId(
        @Path("metricId") metricId: String,
    ): Response<MetricResponse>

    @POST("device/getAllMetricsByDeviceId/{deviceId}")
    suspend fun getAllMetricsByDeviceId(
        @Path("deviceId") deviceId: String,
    ): Response<AllMetricsResponse>

    @GET("device/getWristBandSleepDurationData/{device_id}")
    suspend fun getWristBandSleepDurationData(
        @Path("device_id") deviceId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("offset") offset: String
    ): Response<WristBandSleepDurationResponse>


    // Nuggets
    @POST("nugget/getAllNuggetPreference")
    suspend fun getNuggetsPreference(): Response<NuggetPreference>

    @GET("nuggetTag/getNuggetTagByTypeAndLevel")
    suspend fun getNuggetsTypeAndLevel(): Response<NuggetsTypeAndLevelResponse>

    @POST("nuggetTag/createUserNuggetTagPreference")
    suspend fun createNuggetPreference(@Body param: CreateNuggetPrefParam): Response<Any>

    @POST("nugget/nuggetLikeDislike/{nuggetId}/{anecdoteId}")
    suspend fun nuggetsInteraction(
        @Path("nuggetId") nuggetId: String,
        @Path("anecdoteId") anecdoteId: String?,
        @Body param: NuggetsInteraction,
    ): Response<NuggetsReactionResponse>

    @GET("nugget/getNuggetById/{nuggetId}")
    suspend fun getNuggetDetails(
        @Path("nuggetId") nuggetId: String,
    ): Response<NuggetDetailResponse>


    @POST("book/bookLikeById/{bookId}")
    suspend fun likeBook(
        @Path("bookId") bookId: String,
    ): Response<BookLikeResponse>

    @POST("cart/createCart")
    suspend fun addToCart(
        @Body bookId: AddToCartParam,
    ): Response<AddToCartResponse>


    @GET("book/getBookByUserPreference")
    suspend fun getBookDetail(): Response<BookPreferenceResponse>

    @GET("nugget/nuggetProgressAPI")
    suspend fun getBioHackProgress(): Response<BioHackProgressResponse>

    @GET("book/getBookById/{bookId}")
    suspend fun getBookSummary(@Path("bookId") bookId: String): Response<BookDetailResponse>

}