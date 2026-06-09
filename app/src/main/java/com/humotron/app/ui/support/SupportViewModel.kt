package com.humotron.app.ui.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.SupportRepository
import com.humotron.app.domain.modal.response.SupportHomeResponse
import com.humotron.app.domain.modal.response.MyTicketsResponse
import com.humotron.app.domain.modal.response.SearchTopicsResponse
import com.humotron.app.domain.modal.response.SearchTopicItem
import com.humotron.app.domain.modal.response.SupportTopicDetailResponse
import com.humotron.app.domain.modal.response.TopicsByCategoryResponse
import com.humotron.app.domain.modal.response.CommonResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository
) : ViewModel() {

    private val _supportHomeData = MutableLiveData<Resource<SupportHomeResponse>>()
    val supportHomeData: LiveData<Resource<SupportHomeResponse>> get() = _supportHomeData

    private val _myTicketsData = MutableLiveData<Resource<MyTicketsResponse>>()
    val myTicketsData: LiveData<Resource<MyTicketsResponse>> get() = _myTicketsData

    private val _searchTopicsData = MutableLiveData<Resource<SearchTopicsResponse>>()
    val searchTopicsData: LiveData<Resource<SearchTopicsResponse>> get() = _searchTopicsData

    private val _topicDetailData = MutableLiveData<Resource<SupportTopicDetailResponse>>()
    val topicDetailData: LiveData<Resource<SupportTopicDetailResponse>> get() = _topicDetailData

    private val _topicsByCategoryData = MutableLiveData<Resource<TopicsByCategoryResponse>>()
    val topicsByCategoryData: LiveData<Resource<TopicsByCategoryResponse>> get() = _topicsByCategoryData

    private val _supportCategoryData = MutableLiveData<Resource<TopicsByCategoryResponse>>()
    val supportCategoryData: LiveData<Resource<TopicsByCategoryResponse>> get() = _supportCategoryData
    
    private val _saveTicketData = MutableLiveData<Resource<CommonResponse>>()
    val saveTicketData: LiveData<Resource<CommonResponse>> get() = _saveTicketData

    private val _allTopicsData = MutableLiveData<Resource<com.humotron.app.domain.modal.response.AllTopicsResponse>>()
    val allTopicsData: LiveData<Resource<com.humotron.app.domain.modal.response.AllTopicsResponse>> get() = _allTopicsData

    private var searchJob: kotlinx.coroutines.Job? = null

    private var currentPage = 1
    private var totalPages = 1
    private var currentQuery = ""
    private val accumulatedTopics = mutableListOf<SearchTopicItem>()
    private var isPageLoading = false
    private var totalTopicsCount = 0

    private var allTopicsCurrentPage = 1
    private var allTopicsTotalRecords = 0
    private var allTopicsLimit = 20
    private val accumulatedAllTopics = mutableListOf<SearchTopicItem>()
    private var isAllTopicsLoading = false

    fun fetchSupportHomeData() {
        supportRepository.getSupportHome().onEach { resource ->
            _supportHomeData.value = resource
        }.launchIn(viewModelScope)
    }

    fun fetchMyTickets() {
        supportRepository.getMyTickets().onEach { resource ->
            _myTicketsData.value = resource
        }.launchIn(viewModelScope)
    }

    fun fetchTopicDetail(topicId: String) {
        supportRepository.getTopicDetail(topicId).onEach { resource ->
            _topicDetailData.value = resource
        }.launchIn(viewModelScope)
    }

    fun searchTopics(query: String) {
        searchJob?.cancel()
        val trimmedQuery = query.trim()
        if (trimmedQuery.length <= 1) {
            currentQuery = ""
            currentPage = 1
            totalPages = 1
            accumulatedTopics.clear()
            isPageLoading = false
            _searchTopicsData.value = Resource.success(null)
            return
        }

        currentQuery = trimmedQuery
        currentPage = 1
        totalPages = 1
        accumulatedTopics.clear()
        isPageLoading = false

        searchJob = viewModelScope.launch {
            delay(300) // Debounce delay of 300ms
            supportRepository.searchTopics(page = 1, query = trimmedQuery, limit = 10)
                .collect { resource ->
                    when (resource.status) {
                        Status.LOADING -> {
                            isPageLoading = true
                            _searchTopicsData.value = resource
                        }
                        Status.SUCCESS -> {
                            isPageLoading = false
                            val response = resource.data
                            val pagination = response?.data?.pagination
                            totalPages = pagination?.pages ?: 1
                            currentPage = pagination?.page ?: 1

                            val topics = response?.data?.topics ?: emptyList()
                            accumulatedTopics.clear()
                            accumulatedTopics.addAll(topics)
                            totalTopicsCount = response?.data?.counts?.topics ?: topics.size

                            _searchTopicsData.value = Resource.success(response)
                        }
                        Status.ERROR, Status.EXCEPTION -> {
                            isPageLoading = false
                            _searchTopicsData.value = resource
                        }
                    }
                }
        }
    }

    fun loadMoreSearchTopics() {
        if (isPageLoading || currentPage >= totalPages || currentQuery.isEmpty()) return

        isPageLoading = true

        viewModelScope.launch {
            supportRepository.searchTopics(page = 1, query = currentQuery, limit = 1000)
                .collect { resource ->
                    when (resource.status) {
                        Status.LOADING -> {
                            // Forward loading status to trigger UI spinner
                            _searchTopicsData.value = Resource.loading()
                        }
                        Status.SUCCESS -> {
                            isPageLoading = false
                            val response = resource.data
                            val pagination = response?.data?.pagination
                            currentPage = pagination?.page ?: 1
                            totalPages = pagination?.pages ?: 1

                            val newTopics = response?.data?.topics ?: emptyList()
                            accumulatedTopics.clear()
                            accumulatedTopics.addAll(newTopics)

                            // Construct combined response with accumulated topics
                            val combinedResponse = response?.copy(
                                data = response.data?.copy(
                                    topics = ArrayList(accumulatedTopics)
                                )
                            )
                            _searchTopicsData.value = Resource.success(combinedResponse)
                        }
                        Status.ERROR, Status.EXCEPTION -> {
                            isPageLoading = false
                            // Emit error with message
                            _searchTopicsData.value = Resource(
                                Status.ERROR,
                                null,
                                resource.error
                            )
                        }
                    }
                }
        }
    }

    fun hasMorePages(): Boolean = currentPage < totalPages

    fun isLoadingPage(): Boolean = isPageLoading

    fun getTotalTopicsCount(): Int = totalTopicsCount

    fun getCurrentPage(): Int = currentPage

    fun fetchTopicsByCategory(categoryKey: String) {
        supportRepository.getTopicsByCategory(categoryKey).onEach { resource ->
            _topicsByCategoryData.value = resource
        }.launchIn(viewModelScope)
    }

    fun fetchSupportCategoryByKey(categoryKey: String) {
        supportRepository.getSupportCategoryByKey(categoryKey).onEach { resource ->
            _supportCategoryData.value = resource
        }.launchIn(viewModelScope)
    }

    fun clearAllTopicsData() {
        allTopicsCurrentPage = 1
        allTopicsTotalRecords = 0
        accumulatedAllTopics.clear()
        isAllTopicsLoading = false
        _allTopicsData.value = null
    }

    fun fetchAllTopics(isInitialLoad: Boolean = true) {
        android.util.Log.d("PaginationTest", "fetchAllTopics called: isInitialLoad: $isInitialLoad, allTopicsCurrentPage: $allTopicsCurrentPage, allTopicsTotalRecords: $allTopicsTotalRecords, accumulatedSize: ${accumulatedAllTopics.size}, isAllTopicsLoading: $isAllTopicsLoading")
        if (isInitialLoad) {
            allTopicsCurrentPage = 1
            allTopicsTotalRecords = 0
            accumulatedAllTopics.clear()
            _allTopicsData.value = Resource.loading()
        } else {
            if (isAllTopicsLoading || (allTopicsTotalRecords > 0 && accumulatedAllTopics.size >= allTopicsTotalRecords)) {
                android.util.Log.d("PaginationTest", "Blocked fetch: isAllTopicsLoading: $isAllTopicsLoading, hasReachedEnd: ${allTopicsTotalRecords > 0 && accumulatedAllTopics.size >= allTopicsTotalRecords}")
                return
            }
        }

        isAllTopicsLoading = true
        viewModelScope.launch {
            supportRepository.getAllTopics(limit = allTopicsLimit, page = allTopicsCurrentPage)
                .collect { resource ->
                    android.util.Log.d("PaginationTest", "Collected resource: status: ${resource.status}, data: ${resource.data != null}")
                    when (resource.status) {
                        Status.LOADING -> {
                            if (isInitialLoad) {
                                _allTopicsData.value = resource
                            }
                        }
                        Status.SUCCESS -> {
                            isAllTopicsLoading = false
                            val response = resource.data
                            android.util.Log.d("PaginationTest", "API Success! status: ${response?.status}, topicsCount: ${response?.data?.topics?.size}")
                            if (response?.status == "success" && response.data != null) {
                                allTopicsTotalRecords = response.data.totalRecords ?: 0
                                val newTopics = response.data.topics ?: emptyList()
                                if (isInitialLoad) {
                                    accumulatedAllTopics.clear()
                                }
                                accumulatedAllTopics.addAll(newTopics)
                                allTopicsCurrentPage++

                                val combinedResponse = response.copy(
                                    data = response.data.copy(
                                        topics = ArrayList(accumulatedAllTopics)
                                    )
                                )
                                _allTopicsData.value = Resource.success(combinedResponse)
                            } else {
                                _allTopicsData.value = resource
                            }
                        }
                        Status.ERROR, Status.EXCEPTION -> {
                            isAllTopicsLoading = false
                            android.util.Log.d("PaginationTest", "API Error/Exception: message: ${resource.error?.errorMessage}")
                            _allTopicsData.value = resource
                        }
                    }
                }
        }
    }

    fun hasMoreAllTopics(): Boolean {
        return allTopicsTotalRecords > 0 && accumulatedAllTopics.size < allTopicsTotalRecords
    }

    fun isAllTopicsLoadingPage(): Boolean = isAllTopicsLoading

    fun saveTicket(
        category: String,
        contactReasonCode: String,
        subject: String,
        description: String,
        currentScreen: String,
        source: String,
        osPlatform: String,
        appVersion: String,
        deviceMetaSnapshot: String,
        attachments: List<MultipartBody.Part> = emptyList()
    ) {
        supportRepository.saveTicket(
            category, contactReasonCode, subject, description, currentScreen,
            source, osPlatform, appVersion, deviceMetaSnapshot, attachments
        ).onEach { resource ->
            _saveTicketData.value = resource
        }.launchIn(viewModelScope)
    }
}
