package com.humotron.app.data.local.entity.bpmachine

data class EcgDiagnosisEntity(
    val isRegular: Boolean,
    val isPoorSignal: Boolean,
    val isLeadOff: Boolean,
    val isFastHr: Boolean,
    val isSlowHr: Boolean,
    val isIrregular: Boolean,
    val isPvcs: Boolean,
    val isHeartPause: Boolean,
    val isFibrillation: Boolean,
    val isWideQrs: Boolean,
    val isProlongedQtc: Boolean,
    val isShortQtc: Boolean,
    val resultMess: String,
)