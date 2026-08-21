package com.humotron.app.ui.track

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.DecodeRepository
import com.humotron.app.domain.modal.response.GroupedMetricsDetailsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupedMetricsDetailsViewModel @Inject constructor(
    private val repository: DecodeRepository
) : ViewModel() {

    private val _groupedMetricsDetails = MutableLiveData<Resource<GroupedMetricsDetailsResponse>>()
    val groupedMetricsDetails: LiveData<Resource<GroupedMetricsDetailsResponse>> = _groupedMetricsDetails

    fun getMetricCategoryDetailsById(categoryId: String) {
        viewModelScope.launch {
            repository.getMetricCategoryDetailsById(categoryId).collect {
                _groupedMetricsDetails.postValue(it)
            }
        }
    }
}
