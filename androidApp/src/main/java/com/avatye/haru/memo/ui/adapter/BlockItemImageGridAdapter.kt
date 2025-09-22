package com.avatye.haru.memo.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.data.entity.BitmapImage
import com.avatye.haru.memo.data.entity.ImageTag
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.databinding.ItemBlockImageGridBinding
import com.avatye.haru.memo.ui.ImageDetailActivity
import com.bumptech.glide.Glide

internal class BlockItemImageGridAdapter(
    val activity: Activity,
    private val parentAdapter: MemoBlockAdapter
) : ListAdapter<BitmapImage, BlockItemImageGridAdapter.ItemGridImageHolder>(BlockBitmapImageDiffCallback()) {

    private var onImageListChanged: ((List<String>) -> Unit)? = null  // Bitmap → String
    private var sessionId: String = ""

    private var selectionMode: Boolean = false
    private val selectedPositions = mutableSetOf<Int>()
    private var onSelectionChanged: ((count: Int) -> Unit)? = null

    companion object {
        private const val NAME = "BlockItemImageGridAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemGridImageHolder {
        val binding = ItemBlockImageGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemGridImageHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ItemGridImageHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int = currentList.size

    inner class ItemGridImageHolder(val binding: ItemBlockImageGridBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun handleCellClick(item: BitmapImage) {
            val pos = bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return

            LogTrack.i { "$NAME -> ItemGridImageHolder -> handleCellClick -> { selectionMode: $selectionMode, item.mode: ${item.mode} }" }

            if (selectionMode && (item.mode == MemoMode.MODIFY_MEMO || item.mode == MemoMode.CREATE_MEMO)) {
                toggleSelect(pos)
            } else {
                // 선택모드가 아니면 상세 보기
                val allUris = parentAdapter.getAllImageUrisInMemo()
                val startIndex = allUris.indexOf(item.uri).takeIf { it >= 0 } ?: 0
                ImageDetailActivity.start(
                    activity = activity,
                    mode = item.mode,
                    startIndex = startIndex,
                    uris = allUris, // bitmaps → uris
                    onImageDelete = { deleteImage(pos) }
                )
            }
        }

        fun bind(item: BitmapImage) {
            // 썸네일 로드 (Glide)
            with(binding.ivGridImage) {
                Glide.with(context)
                    .load(item.uri)   // Bitmap → Uri
                    .centerCrop()
                    .dontAnimate()
                    .into(this)

                tag = ImageTag(sessionId, item.uri) // 세션 ID + uri 저장

                setOnClickListener {
                    handleCellClick(item)
                }
            }

            val editableMode = (item.mode == MemoMode.MODIFY_MEMO || item.mode == MemoMode.CREATE_MEMO)

            // 이미지 삭제 버튼
            with(binding.lyGridImageDeleteContainer) {
                isVisible = !selectionMode && editableMode
                setOnClickListener {
                    deleteImage(bindingAdapterPosition)
                }
            }

            // 이미지 선택 체크박스
            with(binding.lyGridCheckboxContainer) {
                isVisible = selectionMode && editableMode
                isClickable = isVisible
                isFocusable = false

                if (selectionMode) {
                    // 선택모드일 때만 selectedPositions 기준으로 체크 상태 반영
                    binding.cbGridCheckbox.isChecked = selectedPositions.contains(bindingAdapterPosition)
                } else {
                    // 선택모드 해제 상태에서는 항상 false
                    binding.cbGridCheckbox.isChecked = false
                }

                setOnClickListener {
                    handleCellClick(item)
                }
            }
        }
    }

    private fun deleteImage(position: Int) {
        val newList = currentList.toMutableList()
        if (position in newList.indices) {
            newList.removeAt(position)
            submitList(newList)
            onImageListChanged?.invoke(newList.map { it.uri }) // bitmap → uri
        }
    }

    fun setOnImageListChanged(callback: (List<String>) -> Unit) { // Bitmap → String
        onImageListChanged = callback
    }

    fun inflateItemView(parent: ViewGroup): View {
        return ItemBlockImageGridBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
    }

    fun bindItemView(view: View, item: BitmapImage) {
        val binding = ItemBlockImageGridBinding.bind(view)
        Glide.with(binding.ivGridImage.context)
            .load(item.uri) // Bitmap → Uri
            .centerCrop()
            .into(binding.ivGridImage)

        binding.lyGridImageDeleteContainer.isVisible = false
        binding.lyGridCheckboxContainer.isVisible = false
        binding.ivGridImage.setOnClickListener(null)
        binding.lyGridImageDeleteContainer.setOnClickListener(null)
        binding.lyGridCheckboxContainer.setOnClickListener(null)
    }

    fun setOnSelectionChanged(callback: (Int) -> Unit) {
        onSelectionChanged = callback
    }

    fun setSelectionMode(enabled: Boolean) {
        LogTrack.i { "BlockItemImageGridAdapter -> setSelectionMode: $enabled (before=$selectionMode)" }
        selectionMode = enabled
        if (!enabled) {
            selectedPositions.clear()
            onSelectionChanged?.invoke(0)
        }
        notifyDataSetChanged() // 여기서 전체 리바인드 강제
    }

    fun clearSelections() {
        selectedPositions.clear()
        notifyDataSetChanged() // UI 갱신 강제
        onSelectionChanged?.invoke(0)
    }

    private fun toggleSelect(position: Int) {
        if (!selectionMode) return
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        onSelectionChanged?.invoke(selectedPositions.size)
    }

    fun deleteSelected() {
        if (selectedPositions.isEmpty()) return
        val newList = currentList.toMutableList()
        selectedPositions.sortedDescending().forEach { idx ->
            if (idx in newList.indices) newList.removeAt(idx)
        }
        selectedPositions.clear()
        submitList(newList)
        onImageListChanged?.invoke(newList.map { it.uri }) // bitmap → uri
        onSelectionChanged?.invoke(0)
    }

    class BlockBitmapImageDiffCallback : DiffUtil.ItemCallback<BitmapImage>() {
        override fun areItemsTheSame(old: BitmapImage, new: BitmapImage) = old.uri == new.uri
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(old: BitmapImage, new: BitmapImage): Boolean {
            return old.uri == new.uri && old.mode == new.mode
        }
    }
}
