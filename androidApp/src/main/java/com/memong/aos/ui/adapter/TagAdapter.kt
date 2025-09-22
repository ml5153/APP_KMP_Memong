package com.memong.aos.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.data.entity.TagItem
import com.memong.aos.databinding.ItemTagAddBinding
import com.memong.aos.databinding.ItemTagSelectBinding

internal class TagAdapter(
    private val onAddClick: ((String) -> Unit)? = null,
    private val onTagCheckChanged: ((tag: String, isChecked: Boolean) -> Unit)? = null,
    showCheckbox: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<TagItem>()

    var showCheckbox: Boolean = showCheckbox
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submitList(newItems: List<TagItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getSelectedTags(): List<TagItem.SelectableTag> {
        return items.filterIsInstance<TagItem.SelectableTag>().filter { it.isChecked }
    }

    fun updateShowCheckbox(show: Boolean) {
        this.showCheckbox = show
        notifyDataSetChanged()
    }

    fun toggleAllCheckboxes(checked: Boolean) {
        items.forEach {
            if (it is TagItem.SelectableTag) {
                it.isChecked = checked
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TagItem.SelectableTag -> VIEW_TYPE_SELECT
            is TagItem.AddableTag -> VIEW_TYPE_ADD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SELECT -> SelectTagViewHolder(
                ItemTagSelectBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_ADD -> AddTagViewHolder(
                ItemTagAddBinding.inflate(inflater, parent, false)
            )

            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TagItem.SelectableTag -> (holder as SelectTagViewHolder).bind(item)
            is TagItem.AddableTag -> (holder as AddTagViewHolder).bind(item)
        }
    }

    inner class SelectTagViewHolder(private val binding: ItemTagSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TagItem.SelectableTag) {
            binding.textTag.text = item.tag
            binding.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

            binding.checkBox.setOnCheckedChangeListener(null) // 🔒 중복 리스너 방지
            binding.checkBox.isChecked = item.isChecked

            binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                item.isChecked = isChecked
                onTagCheckChanged?.invoke(item.tag, isChecked)
            }

            // 🔄 아이템 클릭으로도 체크 상태 전환
            binding.root.setOnClickListener {
                if (showCheckbox) {
                    val newChecked = !binding.checkBox.isChecked
                    binding.checkBox.isChecked = newChecked
                    // ↑ 위에서 리스너에서 isChecked 처리됨
                }
            }
        }
    }

    inner class AddTagViewHolder(private val binding: ItemTagAddBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TagItem.AddableTag) {
            binding.textNewTag.text = item.tag
            binding.buttonAdd.setOnClickListener {
                item.tag.split("#")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach {
                        onAddClick?.let { it1 ->
                            it1(it)
                        }
                    }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_SELECT = 0
        private const val VIEW_TYPE_ADD = 1
    }
}

