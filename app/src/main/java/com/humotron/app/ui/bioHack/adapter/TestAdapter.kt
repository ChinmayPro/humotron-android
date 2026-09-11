package com.humotron.app.ui.bioHack.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemBiohackTestBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class TestAdapter(
    var onTestClick: ((BioHackProgressResponse.Data.TestPack) -> Unit)? = null
) : RecyclerView.Adapter<TestAdapter.ViewHolder>() {

    var list = arrayListOf<BioHackProgressResponse.Data.TestPack>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackTestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val data = list[position]
        val context = holder.itemView.context
        holder.binding.apply {
            tvTag.text = data.primaryId?.tagName?.uppercase() ?: ""
            tvTitle.text = data.testName
            
            // Difficulty Badge Styling
            val difficulty = data.difficulty ?: "Medium"
            tvLevel.text = difficulty
            when {
                difficulty.contains("Easy", ignoreCase = true) -> {
                    tvLevel.background = ContextCompat.getDrawable(context, R.drawable.bg_difficulty_easy)
                    tvLevel.setTextColor(Color.parseColor("#5FE6A6"))
                }
                difficulty.contains("Hard", ignoreCase = true) -> {
                    tvLevel.background = ContextCompat.getDrawable(context, R.drawable.bg_difficulty_hard)
                    tvLevel.setTextColor(Color.parseColor("#F87171"))
                }
                else -> { // Medium / default
                    tvLevel.background = ContextCompat.getDrawable(context, R.drawable.bg_difficulty_medium)
                    tvLevel.setTextColor(Color.parseColor("#FACC15"))
                }
            }

            val qCount = data.totalQuestion ?: "-"
            tvQuestionNumber.text = "$qCount Q"

            // Button state: 1 = Take test ->, 2 = Retake test ->, 0 = Locked
            val status = data.testStatus ?: 0
            when (status) {
                1 -> {
                    tvTakeTest.text = "Take test →"
                    tvTakeTest.background = ContextCompat.getDrawable(context, R.drawable.bg_shop_add_btn)
                    tvTakeTest.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BEF264"))
                    tvTakeTest.setTextColor(Color.parseColor("#0F241F"))
                    tvTakeTest.isEnabled = true
                }
                2 -> {
                    tvTakeTest.text = "Retake test →"
                    tvTakeTest.background = ContextCompat.getDrawable(context, R.drawable.bg_shop_add_btn)
                    tvTakeTest.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BEF264"))
                    tvTakeTest.setTextColor(Color.parseColor("#0F241F"))
                    tvTakeTest.isEnabled = true
                }
                else -> { // 0 or Locked
                    tvTakeTest.text = "Locked"
                    tvTakeTest.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_locked)
                    tvTakeTest.backgroundTintList = null
                    tvTakeTest.setTextColor(Color.parseColor("#618680"))
                    tvTakeTest.isEnabled = false
                }
            }

            tvTakeTest.setOnClickListener {
                if (status != 0) {
                    onTestClick?.invoke(data)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemBiohackTestBinding) : RecyclerView.ViewHolder(binding.root)

    fun setData(list: List<BioHackProgressResponse.Data.TestPack>) {
        this.list = list as ArrayList<BioHackProgressResponse.Data.TestPack>
        notifyDataSetChanged()
    }
}