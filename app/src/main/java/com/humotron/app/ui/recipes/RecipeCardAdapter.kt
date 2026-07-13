package com.humotron.app.ui.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.humotron.app.R

class RecipeCardAdapter(
    private val onRecipeClicked: (RecipeCard) -> Unit,
    private val onLogRecipe: (RecipeCard) -> Unit,
    private val onToggleFavorite: (RecipeCard) -> Unit
) : ListAdapter<RecipeCard, RecipeCardAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivRecipeImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val tvMetricPill: TextView = itemView.findViewById(R.id.tvMetricPill)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val llIngredients: LinearLayout = itemView.findViewById(R.id.llIngredients)
        private val btnHadThis: LinearLayout = itemView.findViewById(R.id.btnHadThis)
        private val tvHadThis: TextView = itemView.findViewById(R.id.tvHadThis)

        fun bind(item: RecipeCard) {
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .transform(CenterCrop(), RoundedCorners(16))
                .into(ivRecipeImage)

            tvTime.text = itemView.context.getString(R.string.time_min, item.timeMinutes)
            tvMetricPill.text = item.metricPillText
            tvTitle.text = item.title
            tvMeta.text = "${item.difficulty} · ${itemView.context.getString(R.string.kcal_per_serving, item.kcalPerServing)}"

            // Update favorite icon
            if (item.isFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_fav_selected)
                ivFavorite.setColorFilter(itemView.context.getColor(R.color.console_danger_red))
            } else {
                ivFavorite.setImageResource(R.drawable.ic_fav)
                ivFavorite.setColorFilter(itemView.context.getColor(R.color.textColorWhite))
            }

            // Ingredients
            llIngredients.removeAllViews()
            item.ingredients.forEach { ingredient ->
                val chipView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.view_recipe_ingredient_chip, llIngredients, false)
                chipView.findViewById<TextView>(R.id.tvIngredient).text = ingredient
                llIngredients.addView(chipView)
            }

            // Action Button styling depending on logged count
            if (item.logsCount > 0) {
                tvHadThis.text = "Had ${item.logsCount}× · log again"
                btnHadThis.setBackgroundResource(R.drawable.bg_recipe_had_btn_logged)
                tvHadThis.setTextColor(itemView.context.getColor(R.color.insights_green))
            } else {
                tvHadThis.text = itemView.context.getString(R.string.i_had_this)
                btnHadThis.setBackgroundResource(R.drawable.bg_recipe_had_btn_bordered)
                tvHadThis.setTextColor(itemView.context.getColor(R.color.textColorWhite))
            }

            btnHadThis.setOnClickListener {
                onLogRecipe(item)
            }

            ivFavorite.setOnClickListener {
                onToggleFavorite(item)
            }

            itemView.setOnClickListener {
                onRecipeClicked(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RecipeCard>() {
        override fun areItemsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
