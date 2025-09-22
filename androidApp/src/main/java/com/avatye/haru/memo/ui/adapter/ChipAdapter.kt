package com.avatye.haru.memo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.R

internal class ChipAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ChipAdapter.ChipViewHolder>() {

    inner class ChipViewHolder(val chip: com.google.android.material.chip.Chip) :
        RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val chip = inflater.inflate(R.layout.item_chip, parent, false)
                as com.google.android.material.chip.Chip
        return ChipViewHolder(chip)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val label = items[position]
        holder.chip.text = label
        holder.chip.setOnClickListener { onClick(label) }
    }

    override fun getItemCount(): Int = items.size
}