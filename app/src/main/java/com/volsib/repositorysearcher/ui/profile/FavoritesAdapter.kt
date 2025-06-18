package com.volsib.repositorysearcher.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.volsib.repositorysearcher.databinding.ItemFavoriteBinding
import com.volsib.repositorysearcher.models.Repo

class FavoritesAdapter(
    private val onOpenInBrowser: (String) -> Unit,
    private val onRemoveFromFavorites: (Repo) -> Unit
) : ListAdapter<Repo, FavoritesAdapter.ViewHolder>(FavoritesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(repo: Repo) {
            binding.apply {
                tvRepoName.text = repo.name
                tvRepoDescription.text = repo.description
                tvStars.text = repo.stargazersCount.toString()
                tvForks.text = repo.forks.toString()
                tvLanguage.text = repo.language

                btnOpenInBrowser.setOnClickListener {
                    repo.url?.let { url -> onOpenInBrowser(url) }
                }

                btnRemoveFromFavorites.setOnClickListener {
                    onRemoveFromFavorites(repo)
                }
            }
        }
    }

    private class FavoritesDiffCallback : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem == newItem
        }
    }
} 