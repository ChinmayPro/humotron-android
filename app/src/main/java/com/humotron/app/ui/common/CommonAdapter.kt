package com.humotron.app.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class CommonAdapter<M>(
    val context: Context,
    private val inflate: (layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean) -> ViewBinding,
    private val bind: (item: M, binding: ViewBinding, position: Int) -> Unit
) : RecyclerView.Adapter<CommonAdapter.Holder>() {

    // List of items
    private var itemList: ArrayList<M>? = null

    /**
     * Inflates the view binding when creating a new ViewHolder.
     *
     * @param parent The parent view group.
     * @param viewType The type of the view.
     * @return The ViewHolder containing the inflated binding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(inflate(LayoutInflater.from(context), parent, false))
    }

    /**
     * Binds data to the view when recycling a ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val data = itemList!![holder.absoluteAdapterPosition]
        bind(data, (holder).binding, holder.absoluteAdapterPosition)
    }

    /**
     * Interface for simple callbacks related to the adapter.
     */
    interface SimpleCallback<M> {
        /**
         * Callback triggered when binding data to the ViewHolder.
         *
         * @param holder The ViewHolder.
         * @param m The data model for the item.
         * @param pos The position of the item.
         * @param color The color associated with the item.
         */
        fun onViewBinding(holder: Holder, m: M, pos: Int, color: Int) {}
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The number of items in the list.
     */
    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    /**
     * ViewHolder class that holds the view binding for each item.
     *
     * @param binding The view binding.
     */
    class Holder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Sets the list of items and notifies the adapter of the change.
     *
     * @param list The list of items.
     */
    fun setList(list: ArrayList<M>) {
        this.itemList = list
        notifyDataSetChanged()
    }

    /**
     * Adds a list of items to the existing list and notifies the adapter of the change.
     *
     * @param list The list of items to add.
     */
    fun addList(list: ArrayList<M>) {
        this.itemList?.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * Deletes an item at the specified position from the list and notifies the adapter of the change.
     *
     * @param deletePosition The position of the item to delete.
     */
    fun deleteItemAt(deletePosition: Int) {
        if (deletePosition < (itemList?.size ?: 0)) {
            itemList?.removeAt(deletePosition)
            notifyDataSetChanged()
        }
    }

    /**
     * Retrieves the list of items.
     *
     * @return The list of items.
     */
    fun getItemList(): ArrayList<M>? {
        return itemList
    }
}
