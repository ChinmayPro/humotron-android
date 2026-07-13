package com.humotron.app.ui.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R

class RecipeCategoryAdapter(
    private val onRecipeClicked: (RecipeCard) -> Unit,
    private val onLogRecipe: (RecipeCard) -> Unit,
    private val onToggleFavorite: (RecipeCard) -> Unit
) : ListAdapter<RecipeCategory, RecipeCategoryAdapter.ViewHolder>(DiffCallback) {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryTitle: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        private val tvCategoryCount: TextView = itemView.findViewById(R.id.tvCategoryCount)
        private val rvRecipes: RecyclerView = itemView.findViewById(R.id.rvRecipes)

        fun bind(item: RecipeCategory) {
            tvCategoryTitle.text = item.title
            tvCategoryCount.text = itemView.context.getString(R.string.suggested, item.recipes.size.toString())

            val childAdapter = RecipeCardAdapter(onRecipeClicked, onLogRecipe, onToggleFavorite)
            rvRecipes.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = childAdapter
                setRecycledViewPool(viewPool)
            }
            childAdapter.submitList(item.recipes)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RecipeCategory>() {
        override fun areItemsTheSame(oldItem: RecipeCategory, newItem: RecipeCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeCategory, newItem: RecipeCategory): Boolean {
            return oldItem == newItem
        }
    }
}
