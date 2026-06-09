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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    private var searchJob: kotlinx.coroutines.Job? = null

    private var currentPage = 1
    private var totalPages = 1
    private var currentQuery = ""
    private val accumulatedTopics = mutableListOf<SearchTopicItem>()
    private var isPageLoading = false
    private var totalTopicsCount = 0

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
}
