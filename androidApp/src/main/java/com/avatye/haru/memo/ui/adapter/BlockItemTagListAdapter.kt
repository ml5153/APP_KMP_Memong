package com.avatye.haru.memo.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.databinding.ItemBlockTagListBinding

class BlockItemTagListAdapter(
    private val activity: Activity
) : ListAdapter<String, BlockItemTagListAdapter.BlockItemTagListViewHolder>(BlockTagDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockItemTagListViewHolder {
        val binding = ItemBlockTagListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockItemTagListViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: BlockItemTagListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    inner class BlockItemTagListViewHolder(val binding: ItemBlockTagListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            with(binding.tvTagItem) {
                text = item
            }
        }
    }


    class BlockTagDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }


}