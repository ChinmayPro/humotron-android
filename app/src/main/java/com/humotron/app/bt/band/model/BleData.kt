package com.humotron.app.bt.band.model

data class BleData(
    var value: ByteArray? = null,
    var action: String? = null,
    var data: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BleData
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false
        if (action != other.action) return false
        if (data != other.data) return false
        return true
    }

    override fun hashCode(): Int {
        var result = value?.contentHashCode() ?: 0
        result = 31 * result + (action?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }
}
