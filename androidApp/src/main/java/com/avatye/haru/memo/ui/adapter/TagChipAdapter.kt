package com.avatye.haru.memo.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.entity.TagEntity

internal class TagChipAdapter(
    private val context: Context?,
    private val onTagClick: (String) -> Unit
) : ListAdapter<TagEntity, TagChipAdapter.TagViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_chip, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(tag: TagEntity) {
            val textView = itemView.findViewById<TextView>(R.id.textTag)

            val fullText = context?.getString(R.string.haru_tag_format, tag.tagName)
            val limitedText = fullText?.length?.let {
                if (it > 6) {
                    fullText.take(6) + "…"
                } else {
                    fullText
                }
            }

            textView.text = limitedText

            textView.setOnClickListener {
                onTagClick.invoke(tag.tagName)  // 클릭 시엔 원본 전체 전달
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TagEntity>() {
        override fun areItemsTheSame(old: TagEntity, new: TagEntity): Boolean =
            old._id == new._id

        override fun areContentsTheSame(old: TagEntity, new: TagEntity): Boolean =
            old == new
    }
}
