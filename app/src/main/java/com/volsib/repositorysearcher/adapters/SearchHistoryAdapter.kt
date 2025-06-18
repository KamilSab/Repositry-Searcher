package com.volsib.repositorysearcher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.volsib.repositorysearcher.databinding.ItemSearchHistoryBinding
import com.volsib.repositorysearcher.models.SearchHistory
import java.text.SimpleDateFormat
import java.util.*

class SearchHistoryAdapter : ListAdapter<SearchHistory, SearchHistoryAdapter.SearchHistoryViewHolder>(SearchHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchHistoryViewHolder(
        private val binding: ItemSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun bind(searchHistory: SearchHistory) {
            binding.apply {
                tvQuery.text = searchHistory.query
                tvDate.text = dateFormat.format(Date(searchHistory.searchDate))
                tvResultsCount.text = "Найдено: ${searchHistory.resultsCount}"
            }
        }
    }

    private class SearchHistoryDiffCallback : DiffUtil.ItemCallback<SearchHistory>() {
        override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem == newItem
        }
    }
} 