package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemRingBandScanBinding

class HealthScanAdapter(
    private val onScanNowClick: (HealthScanItem) -> Unit,
    private val onPastScansClick: (HealthScanItem) -> Unit,
) : ListAdapter<HealthScanItem, HealthScanAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRingBandScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val vBinding: ItemRingBandScanBinding) :
        RecyclerView.ViewHolder(vBinding.root) {
        fun bind(item: HealthScanItem) {
            vBinding.tvScanTitle.text = item.title
            vBinding.tvScanSubtitle.text = item.description

            // Set Icon and Color based on type
            val context = vBinding.root.context
            when (item.type) {
                HealthScanType.HRV -> {
                    vBinding.ivScan.setImageResource(R.drawable.ic_heart_2)
                    vBinding.viewAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.series
                        )
                    )
                    vBinding.ivScan.setColorFilter(ContextCompat.getColor(context, R.color.series))
                    vBinding.ivChevron.setColorFilter(ContextCompat.getColor(context, R.color.series))
                    vBinding.iconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.series_14)
                    )
                    vBinding.tvScanNow.setTextColor(ContextCompat.getColor(context, R.color.series))
                }

                HealthScanType.HR -> {
                    vBinding.ivScan.setImageResource(R.drawable.ic_wave_2)
                    vBinding.viewAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.watch
                        )
                    )
                    vBinding.ivScan.setColorFilter(ContextCompat.getColor(context, R.color.watch))
                    vBinding.ivChevron.setColorFilter(ContextCompat.getColor(context, R.color.watch))
                    vBinding.iconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.watch_14)
                    )
                    vBinding.tvScanNow.setTextColor(ContextCompat.getColor(context, R.color.watch))
                }

                HealthScanType.SPO2 -> {
                    vBinding.ivScan.setImageResource(R.drawable.ic_metrics_o2)
                    vBinding.viewAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.cool
                        )
                    )
                    vBinding.ivScan.setColorFilter(ContextCompat.getColor(context, R.color.cool))
                    vBinding.ivChevron.setColorFilter(ContextCompat.getColor(context, R.color.cool))
                    vBinding.iconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.cool_14)
                    )
                    vBinding.tvScanNow.setTextColor(ContextCompat.getColor(context, R.color.cool))
                }

                HealthScanType.TEMPERATURE -> {
                    vBinding.ivScan.setImageResource(R.drawable.ic_metrics_thermo)
                    vBinding.viewAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.series
                        )
                    )
                    vBinding.ivScan.setColorFilter(ContextCompat.getColor(context, R.color.series))
                    vBinding.ivChevron.setColorFilter(ContextCompat.getColor(context, R.color.series))
                    vBinding.iconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.series_14)
                    )
                    vBinding.tvScanNow.setTextColor(ContextCompat.getColor(context, R.color.series))
                }
            }

            vBinding.tvScanNow.setOnClickListener {
                onScanNowClick(item)
            }
            vBinding.ivChevron.setOnClickListener {
                onScanNowClick(item)
            }

            vBinding.tvLastLabel.setOnClickListener {
                onPastScansClick(item)
            }
            vBinding.tvLastValue.setOnClickListener {
                onPastScansClick(item)
            }
            vBinding.tvLastDate.setOnClickListener {
                onPastScansClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HealthScanItem>() {
        override fun areItemsTheSame(oldItem: HealthScanItem, newItem: HealthScanItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: HealthScanItem, newItem: HealthScanItem): Boolean {
            return oldItem == newItem
        }
    }
}
