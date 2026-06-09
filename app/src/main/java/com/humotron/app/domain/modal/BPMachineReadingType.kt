package com.humotron.app.domain.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class BPMachineReadingType : Parcelable {
    BLOOD_PRESSURE,
    ECG
}
