package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.WristBandMetricDetail
import com.humotron.app.util.toTitleFromCamelCase
import com.humotron.app.util.utcOffsetToLocalTime
import com.humotron.app.util.utcOffsetToOrdinalDate

class SleepMetricsAdapter(private val onClick: (WristBandMetricDetail, String) -> Unit) :
    ListAdapter<WristBandMetricDetail, SleepMetricsAdapter.HardwareVH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HardwareVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sleep_metrics, parent, false)
        return HardwareVH(view)
    }

    override fun onBindViewHolder(holder: HardwareVH, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class HardwareVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvType: TextView = view.findViewById(R.id.tvType)
        private val tvValue: TextView = view.findViewById(R.id.tvValue)
        private val tvMetricUnit: TextView = view.findViewById(R.id.tvMetricUnit)
        private val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        private val mcvMetrics: View = view.findViewById(R.id.mcv_metrics)

        fun bind(
            item: WristBandMetricDetail,
            onClick: (WristBandMetricDetail, String) -> Unit,
        ) {
            tvType.text = item.metricValue?.fieldLabel?.toTitleFromCamelCase()
            tvValue.text = formatValue(item.metricValue?.value)
            tvMetricUnit.text = item.metricUnit
            val dateTime = item.metricValue?.timestamp ?: ""
            val formattedDateTime = utcOffsetToOrdinalDate(dateTime)
            tvDateTime.text = formattedDateTime
            mcvMetrics.setOnClickListener { onClick(item, dateTime) }
        }

        fun formatValue(value: Any?): String {
            if (value == null) return "-"
            val doubleValue = value.toString().toDoubleOrNull()
            return if (doubleValue != null) {
                if (doubleValue % 1.0 == 0.0) {
                    doubleValue.toInt().toString()
                } else {
                    doubleValue.toString()
                }
            } else {
                value.toString()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WristBandMetricDetail>() {
        override fun areItemsTheSame(
            oldItem: WristBandMetricDetail,
            newItem: WristBandMetricDetail,
        ) = oldItem._id == newItem._id

        override fun areContentsTheSame(
            oldItem: WristBandMetricDetail,
            newItem: WristBandMetricDetail,
        ) = oldItem == newItem
    }
}
