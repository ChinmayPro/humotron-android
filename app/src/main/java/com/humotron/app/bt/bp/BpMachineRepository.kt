package com.humotron.app.bt.bp

import com.humotron.app.data.local.dao.bpmachine.BpMachineDao
import com.humotron.app.data.local.entity.bpmachine.BpData
import com.humotron.app.data.local.entity.bpmachine.EcgData
import com.humotron.app.data.local.entity.bpmachine.EcgDiagnosisEntity
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.BPMachineData
import com.humotron.app.domain.modal.param.BPMachineDiagnosisData
import com.humotron.app.domain.modal.param.BPMachineUploadRequest
import com.humotron.app.util.formatMillisToIsoUtc
import com.lepu.blepro.ext.bp2.Bp2File
import com.lepu.blepro.ext.bp2.BpFile
import com.lepu.blepro.ext.bp2.EcgFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class BpSyncStatus {
    IDLE,
    FETCHING_FILE_LIST,
    SYNCING_FILES,
    COMPLETED,
    FAILED
}

@Singleton
class BpMachineRepository @Inject constructor(
    private val bpMachineDao: BpMachineDao,
    private val api: AppApi,
    private val responseHandler: ResponseHandler,
) {
    suspend fun getSyncedFileNames(): Set<String> = withContext(Dispatchers.IO) {
        val bpFiles = bpMachineDao.getAllBpFileNames()
        val ecgFiles = bpMachineDao.getAllEcgFileNames()
        (bpFiles + ecgFiles).toSet()
    }

    suspend fun saveBp2File(model: Int, hardwareId: String, bp2File: Bp2File) =
        withContext(Dispatchers.IO) {
            when (bp2File.type) {
                1 -> { // BP Data
                    val bpFile = BpFile(bp2File.content)
                    val bpData = BpData(
                        hardwareId = hardwareId,
                        measureTime = bpFile.measureTime,
                        sys = bpFile.sys,
                        dia = bpFile.dia,
                        mean = bpFile.mean,
                        pr = bpFile.pr,
                        arrhythmia = bpFile.isArrhythmia,
                        fileName = bp2File.fileName,
                        sync = false
                    )
                    bpMachineDao.insertBpData(bpData)
                }

                2 -> { // ECG Data
                    val ecgFile = EcgFile(bp2File.content)
                    val ecgData = EcgData(
                        hardwareId = hardwareId,
                        measureTime = ecgFile.measureTime,
                        date = formatMillisToIsoUtc(ecgFile.measureTime * 1000L),
                        fileVersion = ecgFile.fileVersion,
                        fileType = ecgFile.fileType,
                        recordingTime = ecgFile.recordingTime,
                        result = ecgFile.result,
                        hr = ecgFile.hr,
                        qrs = ecgFile.qrs,
                        pvcs = ecgFile.pvcs,
                        qtc = ecgFile.qtc,
                        connectCable = ecgFile.isConnectCable,
                        fileName = bp2File.fileName,
                        diagnosis = EcgDiagnosisEntity(
                            isRegular = ecgFile.diagnosis.isRegular,
                            isPoorSignal = ecgFile.diagnosis.isPoorSignal,
                            isLeadOff = ecgFile.diagnosis.isLeadOff,
                            isFastHr = ecgFile.diagnosis.isFastHr,
                            isSlowHr = ecgFile.diagnosis.isSlowHr,
                            isIrregular = ecgFile.diagnosis.isIrregular,
                            isPvcs = ecgFile.diagnosis.isPvcs,
                            isHeartPause = ecgFile.diagnosis.isHeartPause,
                            isFibrillation = ecgFile.diagnosis.isFibrillation,
                            isWideQrs = ecgFile.diagnosis.isWideQrs,
                            isProlongedQtc = ecgFile.diagnosis.isProlongedQtc,
                            isShortQtc = ecgFile.diagnosis.isShortQtc,
                            resultMess = ecgFile.diagnosis.resultMess
                        ),
                        sync = false
                    )
                    bpMachineDao.insertEcgData(ecgData)
                }
            }
        }

    suspend fun getUnsyncedBpData() = withContext(Dispatchers.IO) {
        bpMachineDao.getUnsyncedBpData()
    }

    suspend fun updateBpSyncStatus(id: Long, synced: Boolean) = withContext(Dispatchers.IO) {
        bpMachineDao.updateBpSyncStatus(id, synced)
    }

    suspend fun getUnsyncedEcgData() = withContext(Dispatchers.IO) {
        bpMachineDao.getUnsyncedEcgData()
    }

    suspend fun updateEcgSyncStatus(id: Long, synced: Boolean) = withContext(Dispatchers.IO) {
        bpMachineDao.updateEcgSyncStatus(id, synced)
    }

    suspend fun syncBpDataToServer(bpData: BpData) = withContext(Dispatchers.IO) {
        val request = BPMachineUploadRequest(
            hardwareId = bpData.hardwareId,
            recordTimestamp = (System.currentTimeMillis() / 1000).toString(),
            data = BPMachineData(
                measureTime = bpData.measureTime,
                systolicPressure = bpData.sys,
                diastolicPressure = bpData.dia,
                meanPressure = bpData.mean,
                pulseRate = bpData.pr,
                arrhythmia = bpData.arrhythmia,
                fileName = bpData.fileName
            )
        )
        try {
            val response = api.sendBPMachineDataToServer(request)
            responseHandler.handleResponse(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun syncEcgDataToServer(ecgData: EcgData) = withContext(Dispatchers.IO) {
        val request = BPMachineUploadRequest(
            hardwareId = ecgData.hardwareId,
            recordTimestamp = (System.currentTimeMillis() / 1000).toString(),
            data = BPMachineData(
                measureTime = ecgData.measureTime,
                date = ecgData.date,
                recordingTime = ecgData.recordingTime,
                result = ecgData.result,
                hr = ecgData.hr,
                qrs = ecgData.qrs,
                pvcs = ecgData.pvcs,
                qtc = ecgData.qtc,
                connectCable = ecgData.connectCable,
                fileName = ecgData.fileName,
                diagnosis = BPMachineDiagnosisData(
                    isRegular = ecgData.diagnosis.isRegular,
                    isPoorSignal = ecgData.diagnosis.isPoorSignal,
                    isLeadOff = ecgData.diagnosis.isLeadOff,
                    isFastHr = ecgData.diagnosis.isFastHr,
                    isSlowHr = ecgData.diagnosis.isSlowHr,
                    isIrregular = ecgData.diagnosis.isIrregular,
                    isPvcs = ecgData.diagnosis.isPvcs,
                    isHeartPause = ecgData.diagnosis.isHeartPause,
                    isFibrillation = ecgData.diagnosis.isFibrillation,
                    isWideQrs = ecgData.diagnosis.isWideQrs,
                    isProlongedQtc = ecgData.diagnosis.isProlongedQtc,
                    isShortQtc = ecgData.diagnosis.isShortQtc,
                    resultMess = ecgData.diagnosis.resultMess
                )
            )
        )
        try {
            val response = api.sendBPMachineDataToServer(request)
            responseHandler.handleResponse(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }
}
