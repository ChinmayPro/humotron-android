package com.humotron.app.ui.bloodTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.network.error.Error
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.ExtractMetricsResponse
import com.humotron.app.domain.modal.response.GenerateMetricResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BloodTestViewModel @Inject constructor(
    private val gmailRepository: com.humotron.app.data.repository.GmailRepository,
    private val medicalRepository: com.humotron.app.data.repository.MedicalRepository
) : ViewModel() {

    private val _accountEmail = MutableLiveData<String?>()
    val accountEmail: LiveData<String?> = _accountEmail

    fun setAccountEmail(email: String?) {
        _accountEmail.value = email
    }

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _pdfResults = MutableLiveData<List<com.humotron.app.data.repository.ClinicalDocumentInfo>>(emptyList())
    val pdfResults: LiveData<List<com.humotron.app.data.repository.ClinicalDocumentInfo>> = _pdfResults

    private val _noResultsEvent = MutableLiveData<Boolean>(false)
    val noResultsEvent: LiveData<Boolean> = _noResultsEvent

    fun onNoResultsShown() {
        _noResultsEvent.value = false
    }

    private val _navigateToImport = MutableLiveData<Boolean>(false)
    val navigateToImport: LiveData<Boolean> = _navigateToImport

    fun onImportNavigated() {
        _navigateToImport.value = false
    }

    fun searchGmail(
        context: android.content.Context,
        accountName: String,
        keywords: List<String>,
        labels: List<String>,
        dateRange: String,
        hasAttachments: Boolean
    ) {
        _noResultsEvent.value = false
        _navigateToImport.value = false
        _error.value = null
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = gmailRepository.searchPdfAttachments(context, accountName, keywords, labels, dateRange, hasAttachments)
                _pdfResults.value = results
                
                if (results.isNotEmpty()) {
                    _navigateToImport.value = true
                } else {
                    _noResultsEvent.value = true
                }
                
                android.util.Log.d("BloodTestViewModel", "Search completed. Found ${results.size} PDF(s).")
                results.forEach { 
                    android.util.Log.d("BloodTestViewModel", "PDF Found: ${it.fileName} (ID: ${it.attachmentId})")
                }
            } catch (e: Exception) {
                android.util.Log.e("BloodTestViewModel", "Search failed: ${e.message}", e)
                _error.value = "Search failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _uploadState = MutableLiveData<Resource<ExtractMetricsResponse>?>()
    val uploadState: LiveData<Resource<ExtractMetricsResponse>?> = _uploadState

    private val _metricState = MutableLiveData<Resource<GenerateMetricResponse>?>()
    val metricState: LiveData<Resource<GenerateMetricResponse>?> = _metricState

    fun setUploadResult(result: ExtractMetricsResponse, initialIndex: Int = 0) {
        _selectedReportIndex.value = initialIndex
        _uploadState.value = Resource.success(result)
    }

    private val _selectedReportIndex = MutableLiveData<Int>(0)
    val selectedReportIndex: LiveData<Int> = _selectedReportIndex

    fun setSelectedReportIndex(index: Int) {
        _selectedReportIndex.value = index
    }

    fun setDevicePdfs(uris: List<android.net.Uri>, context: android.content.Context) {
        val results = uris.map { uri ->
            var fileName = "document.pdf"
            var size = 0L
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = it.getString(nameIndex)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                }
            }
            
            com.humotron.app.data.repository.ClinicalDocumentInfo(
                fileName = fileName,
                mimeType = "application/pdf",
                size = size,
                timestamp = System.currentTimeMillis(),
                uri = uri.toString()
            )
        }
        _error.value = null
        _uploadState.value = null
        
        _pdfResults.value = results
        if (results.isNotEmpty()) {
            _navigateToImport.value = true
        }
    }

    fun uploadSelectedPdfs(
        context: android.content.Context,
        selectedAttachments: List<com.humotron.app.data.repository.ClinicalDocumentInfo>
    ) {
        if (selectedAttachments.isEmpty()) return
        val attachment = selectedAttachments.first()
        
        viewModelScope.launch {
            _isLoading.value = true
            _uploadState.value = Resource.loading()
            try {
                val file = if (attachment.uri != null) {
                    // It's a local uri, copy to cache
                    copyUriToCache(context, android.net.Uri.parse(attachment.uri), attachment.fileName)
                } else {
                    // It's a Gmail attachment
                    val accountName = _accountEmail.value ?: throw Exception("Account not selected")
                    gmailRepository.downloadAttachment(
                        context,
                        accountName,
                        attachment.messageId,
                        attachment.attachmentId,
                        attachment.fileName
                    )
                }

                if (file != null) {
                    medicalRepository.createClinicalDocuments(
                        isCreateNugget = true,
                        uploadType = "MANUAL",
                        file = file
                    ).collect { resource ->
                        _uploadState.value = resource
                        when (resource.status) {
                            com.humotron.app.data.network.Status.SUCCESS -> {
                                android.util.Log.d("BloodTestViewModel", "uploadSelectedPdfs SUCCESS: ${resource.data?.message}")
                                _isLoading.value = false
                            }
                            com.humotron.app.data.network.Status.ERROR -> {
                                android.util.Log.e("BloodTestViewModel", "uploadSelectedPdfs ERROR: ${resource.error?.errorMessage}")
                                _isLoading.value = false
                                _error.value = resource.error?.errorMessage ?: "Upload failed"
                            }
                            com.humotron.app.data.network.Status.EXCEPTION -> {
                                android.util.Log.e("BloodTestViewModel", "uploadSelectedPdfs EXCEPTION: ${resource.error?.errorMessage}")
                                _isLoading.value = false
                                _error.value = resource.error?.errorMessage ?: "Unexpected error"
                            }
                            com.humotron.app.data.network.Status.LOADING -> {
                                android.util.Log.d("BloodTestViewModel", "uploadSelectedPdfs LOADING...")
                                _isLoading.value = true
                            }
                        }
                    }
                } else {
                    _error.value = "Failed to process selected document"
                    _isLoading.value = false
                    _uploadState.value = Resource.error(com.humotron.app.data.network.error.Error(errorMessage = "Failed to process document"))
                }
            } catch (e: Exception) {
                _error.value = "Upload failed: ${e.message}"
                _isLoading.value = false
                _uploadState.value = Resource.error(com.humotron.app.data.network.error.Error(errorMessage = e.message ?: "Unknown error"))
            }
        }
    }

    private fun copyUriToCache(context: android.content.Context, uri: android.net.Uri, fileName: String): java.io.File? {
        return try {
            val cacheFile = java.io.File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile
        } catch (e: Exception) {
            null
        }
    }

    fun clearResults() {
        _pdfResults.value = emptyList()
        _noResultsEvent.value = false
        _navigateToImport.value = false
        _uploadState.value = null
        _metricState.value = null
        _error.value = null
        lastParsedPdfId = null
    }

    fun resetUploadState() {
        _uploadState.value = null
        _error.value = null
    }

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var lastParsedPdfId: String? = null

    fun generateMetricByPdfId(pdfId: String) {
        if (pdfId == lastParsedPdfId) {
            android.util.Log.d("BloodTestViewModel", "generateMetricByPdfId: Skipping call, same PDF ID: $pdfId")
            return
        }
        
        lastParsedPdfId = pdfId
        viewModelScope.launch {
            medicalRepository.generateMetricByPdfId(pdfId).collect { resource ->
                _metricState.value = resource
                when (resource.status) {
                    com.humotron.app.data.network.Status.SUCCESS -> {
                        android.util.Log.d("BloodTestViewModel", "generateMetricByPdfId SUCCESS: ${resource.data}")
                    }
                    com.humotron.app.data.network.Status.ERROR -> {
                        android.util.Log.e("BloodTestViewModel", "generateMetricByPdfId ERROR: ${resource.error?.errorMessage}")
                        _error.value = resource.error?.errorMessage ?: "Failed to generate metrics"
                    }
                    com.humotron.app.data.network.Status.EXCEPTION -> {
                        android.util.Log.e("BloodTestViewModel", "generateMetricByPdfId EXCEPTION: ${resource.error?.errorMessage}")
                        _error.value = resource.error?.errorMessage ?: "Unexpected error"
                    }
                    com.humotron.app.data.network.Status.LOADING -> {
                        android.util.Log.d("BloodTestViewModel", "generateMetricByPdfId LOADING...")
                    }
                }
            }
        }
    }
}
