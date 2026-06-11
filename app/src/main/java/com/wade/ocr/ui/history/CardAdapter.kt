package com.wade.ocr.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wade.ocr.data.CardEntity
import com.wade.ocr.databinding.ItemCardBinding

class CardAdapter(private val onItemClick: (CardEntity) -> Unit) : ListAdapter<CardEntity, CardAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(private val binding: ItemCardBinding, private val onClick: (CardEntity) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: CardEntity) {
            binding.textName.text = card.name ?: "Unknown Name"
            binding.textTitleCompany.text = "${card.title ?: ""} ${card.company ?: ""}".trim()
            binding.textCategory.text = card.category
            
            binding.root.setOnClickListener {
                onClick(card)
            }
        }
    }

    class CardDiffCallback : DiffUtil.ItemCallback<CardEntity>() {
        override fun areItemsTheSame(oldItem: CardEntity, newItem: CardEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CardEntity, newItem: CardEntity): Boolean {
            return oldItem == newItem
        }
    }
}

