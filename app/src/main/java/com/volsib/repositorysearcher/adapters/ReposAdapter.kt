package com.volsib.repositorysearcher.adapters

import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.databinding.ItemRepoBinding
import com.volsib.repositorysearcher.models.Repo

class ReposAdapter : RecyclerView.Adapter<ReposAdapter.RepoViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onNameClickListener: ((Repo) -> Unit)? = null
    private var onDownloadClickListener: ((Repo) -> Unit)? = null
    private var onFavoriteClickListener: ((Repo) -> Unit)? = null

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = differ.currentList[position]

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onNameClickListener?.invoke(repo)
            }
        }

        val spannableString = SpannableString(repo.name)
        spannableString.setSpan(clickableSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        holder.binding.apply {
            tvName.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
                isClickable = true
            }
            tvDescription.text = repo.description
            tvLanguage.apply {
                text = repo.language
            }
            tvStars.apply {
                text = repo.stargazersCount.toString()
            }
            tvForks.apply {
                text = repo.forks.toString()
            }
            downloadButton.setOnClickListener {
                onDownloadClickListener?.invoke(repo)
            }
            favoriteButton.apply {
                setIconResource(if (repo.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border)
                setOnClickListener {
                    onFavoriteClickListener?.invoke(repo)
                }
            }
        }
    }

    fun setOnNameClickListener(listener: (Repo) -> Unit) {
        onNameClickListener = listener
    }

    fun setOnDownloadClickListener(listener: (Repo) -> Unit) {
        onDownloadClickListener = listener
    }

    fun setOnFavoriteClickListener(listener: (Repo) -> Unit) {
        onFavoriteClickListener = listener
    }

    class RepoViewHolder(val binding: ItemRepoBinding): RecyclerView.ViewHolder(binding.root)
}