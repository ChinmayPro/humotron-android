package com.humotron.app.data.remote

import com.humotron.app.data.local.entity.UploadData
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.CompleteOnboardingParam
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.DailyCalculatedMetricsParam
import com.humotron.app.domain.modal.param.GetConversationThreadsParam
import com.humotron.app.domain.modal.param.GetConversationsParam
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.domain.modal.param.PostFollowUpConversationParam
import com.humotron.app.domain.modal.param.RemovePdfParam
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.BandUploadData
import com.humotron.app.domain.modal.param.StartNewChatParam
import com.humotron.app.domain.modal.param.SubmitPersonalInfoParam
import com.humotron.app.domain.modal.param.WeightHeightParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.AddDeviceDataResponse
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.modal.response.AddToCartResponse
import com.humotron.app.domain.modal.response.BookAddToCartResponse
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.AssessmentResponse
import com.humotron.app.domain.modal.response.BioHackProgressResponse
import com.humotron.app.domain.modal.response.GetOptimizedRecipeWithMetricsResponse
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.domain.modal.response.BookDetailResponse
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.ConversationThreadsResponse
import com.humotron.app.domain.modal.response.DailyCalculatedMetricsResponse
import com.humotron.app.domain.modal.response.ExtractMetricsResponse
import com.humotron.app.domain.modal.response.FeltOffQuestionsResponse
import com.humotron.app.domain.modal.response.GenerateMetricResponse
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.GetConversationsResponse
import com.humotron.app.domain.modal.response.HardwareListData
import com.humotron.app.domain.modal.response.MedicalPdfResponse
import com.humotron.app.domain.modal.response.MergedAssessmentResponse
import com.humotron.app.domain.modal.response.MetricResponse
import com.humotron.app.domain.modal.response.MetricTrackingResponse
import com.humotron.app.domain.modal.response.NuggetDetailResponse
import com.humotron.app.domain.modal.response.NuggetPreference
import com.humotron.app.domain.modal.response.NuggetsReactionResponse
import com.humotron.app.domain.modal.response.NuggetsTypeAndLevelResponse
import com.humotron.app.domain.modal.response.PostFollowUpConversationResponse
import com.humotron.app.domain.modal.response.PromptContextResponse
import com.humotron.app.domain.modal.response.RingReadingData
import com.humotron.app.domain.modal.response.SubmitAnswerRequest
import com.humotron.app.domain.modal.response.SubmitAnswerResponse
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.UseCaseResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import com.humotron.app.domain.modal.response.WristBandSleepDurationResponse
import com.humotron.app.domain.modal.response.YetToTrackMetricResponse
import com.humotron.app.domain.modal.response.ProductDetailResponse
import com.humotron.app.domain.modal.response.ProductVariantResponse
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.response.ShopAddToCartResponse
import com.humotron.app.domain.modal.response.GetDefaultConfigResponse
import com.humotron.app.domain.modal.param.UpdateAddressRequest
import com.humotron.app.domain.modal.response.UpdateAddressResponse
import com.humotron.app.domain.modal.response.AddressAutocompleteResponse
import com.humotron.app.domain.modal.response.FullAddressResponse
import com.humotron.app.domain.modal.response.GetAllAddressResponse
import com.humotron.app.domain.modal.response.GetAllLabResponse
import com.humotron.app.domain.modal.param.DefaultConfigRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AppApi {

    @Multipart
    @POST("medicalDetails/createClinicalDocuments/{isCreateNugget}")
    suspend fun createClinicalDocuments(
        @Path("isCreateNugget") isCreateNugget: Boolean,
        @Part("uploadType") uploadType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ExtractMetricsResponse>

    @GET("chatFeltOff/questions")
    suspend fun getFeltOffQuestions(): Response<FeltOffQuestionsResponse>

    @GET("chatNutritionIdea/questions")
    suspend fun getNutritionIdeaQuestions(): Response<FeltOffQuestionsResponse>


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

    @POST("hardwareSpecificDetail/addHardwareSpecificDetail")
    suspend fun sendBandDataToServer(@Body data: BandUploadData): Response<AddDeviceDataResponse>

    @POST("userHardware/addUserHardware")
    suspend fun addHardwareId(@Body data: AddHardware): Response<AddHardwareResponse>

    @POST("device/getAllDeviceByUserId")
    suspend fun getAllDeviceData(): Response<GetAllDeviceResponse>

    @POST("device/getAllDeviceWithMetrics")
    suspend fun getAllDeviceWithMetrics(): Response<GetShopDevicesResponse>

    @GET("userHardware/getHardwareListByUserId")
    suspend fun getHardwareList(): Response<HardwareListData>

    @POST("device/getRingReadingData/{deviceId}")
    suspend fun getRingReadingData(
        @Path("deviceId") deviceId: String,
    ): Response<RingReadingData>

    @GET("device/getDeviceDetailsById/{deviceId}")
    suspend fun getDeviceDetailsById(
        @Path("deviceId") deviceId: String,
    ): Response<DeviceDetailResponse>

    @GET("deviceFaq/faqByDeviceId/{deviceId}")
    suspend fun getDeviceFaqs(
        @Path("deviceId") deviceId: String,
    ): Response<DeviceFaqResponse>

    @POST("device/deviceLikeDislike/{deviceId}")
    suspend fun deviceLikeDislike(
        @Path("deviceId") deviceId: String,
        @Body emptyBody: okhttp3.RequestBody = okhttp3.RequestBody.create(null, ByteArray(0))
    ): Response<CommonResponse>

    @POST("product/productLikeDislike/{productId}")
    suspend fun productLikeDislike(
        @Path("productId") productId: String,
        @Body emptyBody: okhttp3.RequestBody = okhttp3.RequestBody.create(null, ByteArray(0))
    ): Response<CommonResponse>

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
    
    @POST("cart/createCart")
    suspend fun createBookCart(
        @Body bookId: AddToCartParam,
    ): Response<BookAddToCartResponse>

    @POST("cart/createCart")
    suspend fun addToCartDevice(
        @Body param: AddToCartParam,
    ): Response<ShopAddToCartResponse>

    @POST("cart/deleteCartItemById/{itemId}")
    suspend fun deleteCartItemById(
        @Path("itemId") itemId: String,
    ): Response<CommonResponse>


    @GET("book/getBookByUserPreference")
    suspend fun getBookDetail(): Response<BookPreferenceResponse>

    @GET("nugget/nuggetProgressAPI")
    suspend fun getBioHackProgress(): Response<BioHackProgressResponse>

    @GET("book/getBookById/{bookId}")
    suspend fun getBookSummary(@Path("bookId") bookId: String): Response<BookDetailResponse>

    @POST("userAssessment/getMergedAssessmentList")
    suspend fun getMergedAssessmentList(): Response<MergedAssessmentResponse>

    @POST("medicalDetails/getAllPdfList")
    suspend fun getAllPdfList(): Response<MedicalPdfResponse>

    @GET("assessment/getAssessmentById/{id}")
    suspend fun getAssessment(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<AssessmentResponse>
    @POST("assessmentsQuestionAnswer/createAssessmentQuestionAnswer")
    suspend fun submitAssessmentAnswers(
        @Header("Authorization") token: String,
        @Body request: SubmitAnswerRequest
    ): Response<SubmitAnswerResponse>

    @POST("conversationThread/getAllConversationThreadsByUserId")
    suspend fun getAllConversationThreads(
        @Body param: GetConversationThreadsParam
    ): Response<ConversationThreadsResponse>

    @POST("conversationThread/deleteAllConversationThreadsByUserId")
    suspend fun deleteAllConversationThreads(): Response<CommonResponse>

    @POST("conversationThread/deleteConversationThreadById/{threadId}")
    suspend fun deleteConversationThread(
        @Path("threadId") threadId: String
    ): Response<CommonResponse>

    @POST("metric/getHealthMetricTrackingByUserId")
    suspend fun getHealthMetricTrackingByUserId(): Response<MetricTrackingResponse>

    @POST("metric/getYetToTrackMetricByUserId")
    suspend fun getYetToTrackMetricByUserId(): Response<YetToTrackMetricResponse>

    @POST("conversation/getConversationsByUserId")
    suspend fun getConversationsByUserId(
        @Body param: GetConversationsParam
    ): Response<GetConversationsResponse>

    @POST("conversation/postFollowUpConversation")
    suspend fun postFollowUpConversation(
        @Body param: PostFollowUpConversationParam
    ): Response<PostFollowUpConversationResponse>

    @POST("conversation/startNewChat")
    suspend fun startNewChat(
        @Body param: StartNewChatParam
    ): Response<PostFollowUpConversationResponse>

    @GET("conversation/getPromptContextByConversationId/{conversationId}")
    suspend fun getPromptContextByConversationId(
        @Path("conversationId") conversationId: String
    ): Response<PromptContextResponse>

    @POST("medicalDetails/generateMetricByPdfId")
    suspend fun generateMetricByPdfId(@Body param: com.humotron.app.domain.modal.param.GenerateMetricParam): Response<GenerateMetricResponse>

    @POST("medicalDetails/removePdfByPdfId")
    suspend fun removePdfByPdfId(@Body param: RemovePdfParam): Response<CommonResponse>

    @POST("cart/getCartByUserId")
    suspend fun getCartByUserId(): Response<GetCartResponse>

    @GET("device/getProductVariantById/{deviceId}")
    suspend fun getProductVariantById(
        @Path("deviceId") deviceId: String
    ): Response<ProductVariantResponse>

    @POST("metric/getOptimizedRecipeWithMetrics")
    suspend fun getOptimizedRecipeWithMetrics(): Response<GetOptimizedRecipeWithMetricsResponse>

    @POST("product/getAllTestBookingsType")
    suspend fun getAllTestBookingsType(): Response<BookingTypeResponse>

    @POST("order/getDefaultConfiguration")
    suspend fun getDefaultConfiguration(
        @Body request: DefaultConfigRequest
    ): Response<GetDefaultConfigResponse>

    @POST("address/getAllAddressByUserId")
    suspend fun getAllAddressByUserId(): Response<GetAllAddressResponse>

    @POST("address/updateAddressById/{addressId}")
    suspend fun updateAddressById(
        @Path("addressId") addressId: String,
        @Body request: UpdateAddressRequest
    ): Response<UpdateAddressResponse>

    @GET("https://api.getaddress.io/autocomplete/{term}")
    suspend fun getAddressAutocomplete(
        @Path("term") term: String,
        @Query("api-key") apiKey: String = "Mh0BQoYe8UeAX5lplKtd1A45644"
    ): Response<AddressAutocompleteResponse>

    @GET("https://api.getaddress.io/get/{id}")
    suspend fun getFullAddress(
        @Path("id") id: String,
        @Query("api-key") apiKey: String = "Mh0BQoYe8UeAX5lplKtd1A45644"
    ): Response<FullAddressResponse>

    @GET("product/getProductById/{productId}")
    suspend fun getProductById(
        @Path("productId") productId: String
    ): Response<ProductDetailResponse>

    @GET("lab/getAllLabName")
    suspend fun getAllLabName(
        @Query("postcode") postcode: String
    ): Response<GetAllLabResponse>
}