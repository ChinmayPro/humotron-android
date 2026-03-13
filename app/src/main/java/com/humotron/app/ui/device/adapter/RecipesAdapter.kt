package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.domain.modal.response.RecipeBundle

class RecipesAdapter :
    ListAdapter<RecipeBundle, RecipesAdapter.RecipesViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipes, parent, false)
        return RecipesViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRecipeBundleName: TextView =
            itemView.findViewById(R.id.tvRecipeBundleName)
        private val tvRecipeBundleDesc: TextView =
            itemView.findViewById(R.id.tvRecipeBundleDesc)
        private val tvRecipeCount: TextView =
            itemView.findViewById(R.id.tvRecipeCount)

        fun bind(recipeBundle: RecipeBundle) {
            tvRecipeBundleName.text = recipeBundle.recipeBundleName
            tvRecipeBundleDesc.text = recipeBundle.recipeBundleDesc
            tvRecipeCount.text = "${recipeBundle.recipeCount}"
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeBundle>() {
        override fun areItemsTheSame(
            oldItem: RecipeBundle,
            newItem: RecipeBundle,
        ): Boolean {
            return oldItem.recipeBundleId == newItem.recipeBundleId
        }

        override fun areContentsTheSame(
            oldItem: RecipeBundle,
            newItem: RecipeBundle,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
