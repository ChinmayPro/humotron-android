package com.ecg.algo

interface OnECGAnalysisResultListener {

    fun onOutputFilteredECGData(data: Int)

    fun onOutputECGResult(realtimeHr: Int)
}