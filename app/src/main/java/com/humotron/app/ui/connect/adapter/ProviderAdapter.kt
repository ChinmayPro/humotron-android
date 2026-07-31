package com.humotron.app.ui.connect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemWearableBinding
import com.humotron.app.databinding.ItemWearableHeaderBinding
import com.humotron.app.domain.modal.response.ProviderResponse

class ProviderAdapter(
    private val onItemClick: (ProviderResponse.Data.Provider) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ProviderItem> = emptyList()
    private var selectedProviderId: String? = null

    sealed class ProviderItem {
        data class Header(val title: String) : ProviderItem()
        data class Provider(val data: ProviderResponse.Data.Provider) : ProviderItem()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PROVIDER = 1
    }

    fun updateData(newProviders: List<ProviderResponse.Data.Provider>) {
        val list = mutableListOf<ProviderItem>()

        val unconnectedProviders = newProviders.filter { it.isConnected != true }

        val recommended = unconnectedProviders.filter { it.isRecommended == true }
        if (recommended.isNotEmpty()) {
            list.add(ProviderItem.Header("Recommended"))
            list.addAll(recommended.map { ProviderItem.Provider(it) })
        }

        val others = unconnectedProviders.filter { it.isRecommended != true }
        if (others.isNotEmpty()) {
            list.add(ProviderItem.Header("All devices"))
            list.addAll(others.map { ProviderItem.Provider(it) })
        }

        items = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProviderItem.Header -> TYPE_HEADER
            is ProviderItem.Provider -> TYPE_PROVIDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemWearableHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemWearableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProviderViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProviderItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is ProviderItem.Provider -> (holder as ProviderViewHolder).bind(item.data)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(private val binding: ItemWearableHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvWearableHeader.text = title
        }
    }

    inner class ProviderViewHolder(val binding: ItemWearableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(provider: ProviderResponse.Data.Provider) {
            val context = binding.root.context
            val isSelected = provider.id == selectedProviderId

            binding.tvWhoopName.text = provider.providerName
            binding.tvWhoopMeta.text = provider.subTitle
            binding.tvWhoopLetter.text = provider.providerName?.firstOrNull()?.toString() ?: ""

            provider.bgColorCode?.let {
                try {
                    binding.cvWhoopMark.setCardBackgroundColor(it.toColorInt())
                } catch (e: Exception) {
                }
            }

            provider.txtColorCode?.let {
                try {
                    binding.tvWhoopLetter.setTextColor(it.toColorInt())
                } catch (e: Exception) {
                }
            }

            binding.vWhoopRadio.isChecked = isSelected

            if (isSelected) {
                binding.mcvWearableRow.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.lime_7)
                )
                binding.mcvWearableRow.strokeColor =
                    ContextCompat.getColor(context, R.color.lime)
            } else {
                binding.mcvWearableRow.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.deep_dives_card)
                )
                binding.mcvWearableRow.strokeColor =
                    ContextCompat.getColor(context, R.color.hair)
            }

            binding.root.setOnClickListener {
                selectedProviderId = provider.id
                notifyDataSetChanged()
                onItemClick(provider)
            }
        }
    }
}
