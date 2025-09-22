package com.avatye.haru.memo.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.R
import com.avatye.haru.memo.ui.custom.menu.MemoMenuView

internal class MemoMenuAdapter(
    private val context: Context,
    private val items: List<String>,
    private val selectedLabel: String? = null,
    private val mode: MemoMenuView.Mode = MemoMenuView.Mode.OPTIONS,
    private val onItemClick: (position: Int, label: String) -> Unit
) : RecyclerView.Adapter<MemoMenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val label = items[position]
        holder.bind(label)
        holder.itemView.setOnClickListener {
            onItemClick(position, label)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.tvLabel)

        fun bind(label: String) {
            labelTextView.text = label

            // 선택된 항목은 파란색 (#0076ED), 아니면 기본 색상
            labelTextView.setTextColor(
                if (label == selectedLabel) ContextCompat.getColor(context, R.color.haru_primary_orange)
                else ContextCompat.getColor(context, R.color.haru_black)
            )

            // MEMO_FONT_SIZE 모드일 경우 텍스트 크기 조절
            labelTextView.textSize = when (mode) {
                MemoMenuView.Mode.MEMO_FONT_SIZE -> when (label) {
                    context.getString(R.string.haru_menu_font_size_xs) -> 12f
                    context.getString(R.string.haru_menu_font_size_s) -> 14f
                    context.getString(R.string.haru_menu_font_size_m) -> 16f
                    context.getString(R.string.haru_menu_font_size_l) -> 18f
                    context.getString(R.string.haru_menu_font_size_xl) -> 20f
                    else -> 15f
                }

                else -> 15f
            }
        }
    }
}