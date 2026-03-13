package com.humotron.app.ui.bioHack.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemSwipableNuggetsCardBinding
import com.humotron.app.domain.modal.response.Nugget
import com.humotron.app.view.StoryProgressView

class CardStackAdapter(
    val onChanged: (String?, String?) -> Unit
) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    private var nuggets: List<Nugget> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemSwipableNuggetsCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nugget = nuggets[position]
        Log.e("TAG", "onBindViewHolder: ${nugget.primaryTag}")
        holder.binding.apply {
            tvCategory.text = nugget.category?.tagName
            tvPrimaryTag.text = nugget.primaryTag?.tagName
            tvLearning.text = nugget.learningLevel
            tvTitle.text = nugget.nuggetTopic

            val anecdoteSize = nugget.anecdotes?.size ?: 0
            if (anecdoteSize > 0) {
                storyProgress.isVisible = true
                previousStory.isVisible = true
                nextStory.isVisible = true
                storyProgress.setSegmentCount(nugget.anecdotes?.size ?: 0)
                storyProgress.setOnProgressChangeListener(object :
                    StoryProgressView.OnProgressChangeListener {
                    override fun onSegmentChanged(segmentIndex: Int) {
                        tvQuestion.text = nugget.anecdotes?.getOrNull(segmentIndex)?.content ?: ""
                        tvSource.text = nugget.anecdotes?.getOrNull(segmentIndex)?.source ?: ""
                        textview1.text = nugget.anecdotes?.getOrNull(segmentIndex)?.tagName ?: ""
                        onChanged(nugget.id, nugget.anecdotes?.getOrNull(segmentIndex)?.tag)
                    }
                })
                previousStory.setOnClickListener {
                    storyProgress.previousSegment()
                }

                nextStory.setOnClickListener {
                    storyProgress.nextSegment()
                }
                storyProgress.setCurrentSegment(0)
            } else {
                storyProgress.isVisible = false
                previousStory.isVisible = false
                nextStory.isVisible = false
            }
        }
    }

    override fun getItemCount(): Int {
        return nuggets.size
    }

    fun setNuggets(spots: List<Nugget>) {
        this.nuggets = spots
        notifyDataSetChanged()
    }

    fun getNuggets(): List<Nugget> {
        return nuggets
    }

    class ViewHolder(val binding: ItemSwipableNuggetsCardBinding) :
        RecyclerView.ViewHolder(binding.root)

}