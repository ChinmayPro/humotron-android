package com.humotron.app.ui.bioHack.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.BioHackRepository
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.domain.modal.response.AddToCartResponse
import com.humotron.app.domain.modal.response.BioHackProgressResponse
import com.humotron.app.domain.modal.response.BookDetailResponse
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.Nugget
import com.humotron.app.domain.modal.response.NuggetDetailResponse
import com.humotron.app.domain.modal.response.NuggetPreference
import com.humotron.app.domain.modal.response.NuggetsReactionResponse
import com.humotron.app.domain.modal.response.NuggetsTypeAndLevelResponse
import com.humotron.app.domain.modal.response.Tag
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NuggetsViewModel @Inject constructor(
    private val repository: BioHackRepository,
) : ViewModel() {

    private val nuggetsPreferenceLiveData: SingleLiveEvent<Resource<NuggetPreference>> =
        SingleLiveEvent()

    fun nuggetsPreferenceData(): SingleLiveEvent<Resource<NuggetPreference>> {
        return nuggetsPreferenceLiveData
    }

    val nuggetsPreference: MutableLiveData<List<Nugget>> = MutableLiveData()

    fun getNuggetsPreference() {
        viewModelScope.launch {
            repository.getNuggetsPreference().onEach { state ->
                nuggetsPreferenceLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }


    private val getNuggetsTypeAndLevelLiveData: SingleLiveEvent<Resource<NuggetsTypeAndLevelResponse>> =
        SingleLiveEvent()

    fun getNuggetsTypeAndLevelData(): SingleLiveEvent<Resource<NuggetsTypeAndLevelResponse>> {
        return getNuggetsTypeAndLevelLiveData
    }


    fun getNuggetsTypeAndLevel() {
        viewModelScope.launch {
            repository.getNuggetsTypeAndLevel().onEach { state ->
                getNuggetsTypeAndLevelLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val nuggetsInteractionLiveData: SingleLiveEvent<Resource<NuggetsReactionResponse>> =
        SingleLiveEvent()

    fun nuggetsInteractionLiveDataData(): SingleLiveEvent<Resource<NuggetsReactionResponse>> {
        return nuggetsInteractionLiveData
    }

    fun nuggetsInteraction(param: NuggetsInteraction) {
        viewModelScope.launch {
            repository.nuggetsInteraction(param).onEach { state ->
                nuggetsInteractionLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    val selectedTags = MutableLiveData<List<Tag>>()

    fun setSelectedTags(filter: List<Tag>) {
        selectedTags.value = filter
    }

    private val createPreferenceLiveData: SingleLiveEvent<Resource<Any>> =
        SingleLiveEvent()

    fun createPreferenceData(): SingleLiveEvent<Resource<Any>> {
        return createPreferenceLiveData
    }

    fun createPreference(param: CreateNuggetPrefParam) {
        viewModelScope.launch {
            repository.createPreference(param).onEach { state ->
                createPreferenceLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val nuggetsDetailLiveData: SingleLiveEvent<Resource<NuggetDetailResponse>> =
        SingleLiveEvent()

    fun nuggetsDetailData(): SingleLiveEvent<Resource<NuggetDetailResponse>> {
        return nuggetsDetailLiveData
    }

    fun getNuggetDetails(string: String) {
        viewModelScope.launch {
            repository.getNuggetDetails(string).onEach { state ->
                nuggetsDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }


    private val likeBookLiveData: SingleLiveEvent<Resource<BookLikeResponse>> =
        SingleLiveEvent()

    fun likeBookData(): SingleLiveEvent<Resource<BookLikeResponse>> {
        return likeBookLiveData
    }

    fun likeBook(string: String) {
        viewModelScope.launch {
            repository.likeBook(string).onEach { state ->
                likeBookLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val addToCartLiveData: SingleLiveEvent<Resource<AddToCartResponse>> =
        SingleLiveEvent()

    fun addToCartData(): SingleLiveEvent<Resource<AddToCartResponse>> {
        return addToCartLiveData
    }

    fun addToCart(param: AddToCartParam) {
        viewModelScope.launch {
            repository.addToCart(param).onEach { state ->
                addToCartLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }


    private val getBookDetailLiveData: SingleLiveEvent<Resource<BookPreferenceResponse>> =
        SingleLiveEvent()

    fun getBookDetailData(): SingleLiveEvent<Resource<BookPreferenceResponse>> {
        return getBookDetailLiveData
    }

    fun getBookDetail() {
        viewModelScope.launch {
            repository.getBookDetail().onEach { state ->
                getBookDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val getBioHackProgressLiveData: SingleLiveEvent<Resource<BioHackProgressResponse>> =
        SingleLiveEvent()

    fun bioHackProgressData(): SingleLiveEvent<Resource<BioHackProgressResponse>> {
        return getBioHackProgressLiveData
    }

    fun getBioHackProgress() {
        viewModelScope.launch {
            repository.getBioHackProgress().onEach { state ->
                getBioHackProgressLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }


    private val getBookSummaryLiveData: SingleLiveEvent<Resource<BookDetailResponse>> =
        SingleLiveEvent()

    fun getBookSummaryData(): SingleLiveEvent<Resource<BookDetailResponse>> {
        return getBookSummaryLiveData
    }

    fun getBookSummary(bookId: String) {
        viewModelScope.launch {
            repository.getBookSummary(bookId).onEach { state ->
                getBookSummaryLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

}