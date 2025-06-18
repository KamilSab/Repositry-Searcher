package com.volsib.repositorysearcher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.volsib.repositorysearcher.databinding.ItemDownloadBinding
import com.volsib.repositorysearcher.models.Repo

class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.DownloadViewHolder>() {
    private val differCallback = object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val repo = differ.currentList[position]

        holder.binding.apply {
            tvName.text = repo.owner?.login
            tvUsername.text = repo.name
        }
    }

    class DownloadViewHolder(val binding: ItemDownloadBinding): RecyclerView.ViewHolder(binding.root)
}